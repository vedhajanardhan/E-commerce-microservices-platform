# Deployment Guide

This platform is built to be deployed two different ways depending on
what you're optimizing for. Both are described below — pick the one
that matches your target.

---

## Option A — Single VM with Docker Compose (AWS EC2 / DigitalOcean Droplet)

This is the path of least resistance: the `docker-compose.yml` at the
repo root already runs the *entire* platform (all 11 services + Postgres
+ Kafka + Redis) as one unit. A single VM with Docker installed can run
it exactly as-is.

**Suggested instance size:** 4 vCPU / 8GB RAM minimum (11 JVMs + Kafka +
Postgres running concurrently). A `t3.large` (AWS) or a 8GB Droplet
(DigitalOcean) is a reasonable starting point for a demo/staging
environment; production traffic would need horizontal scaling (see
Option B) rather than a bigger single box.

### Steps (AWS EC2)

1. Launch an Ubuntu 24.04 EC2 instance, open inbound ports 22 (SSH), 80/443
   (if fronting with a reverse proxy), and 8080 (gateway) in the security group.
2. SSH in and install Docker + Compose plugin:
   ```bash
   curl -fsSL https://get.docker.com | sh
   sudo usermod -aG docker $USER
   # log out and back in for the group change to apply
   ```
3. Clone/upload this repository to the instance.
4. Copy `.env.example` to `.env` and set a **strong, unique** `JWT_SECRET`
   (`openssl rand -hex 32`) — do not ship the placeholder value to production.
5. Start the stack:
   ```bash
   docker compose up -d --build
   ```
6. Verify: `docker compose ps` (all healthy), then `curl http://localhost:8080/actuator/health`.
7. Put a reverse proxy (nginx / Caddy) in front of port 8080 for TLS
   termination and a real domain name. Caddy is the least config for a
   quick HTTPS setup:
   ```
   your-domain.com {
       reverse_proxy localhost:8080
   }
   ```

### Steps (DigitalOcean Droplet)

Identical to EC2 above — DigitalOcean's Marketplace has a
"Docker on Ubuntu" base image that skips the manual Docker install step.

---

## Option B — One Service Per Container on a PaaS (Render / Railway)

Render and Railway both run each service as its own independently
deployed container rather than a monolithic `docker compose up` — closer
to how you'd actually run this in production, and where each service
can scale independently.

### What changes vs. Option A

| Concern | Docker Compose (Option A) | Render / Railway (Option B) |
|---|---|---|
| Postgres | one container, multiple DBs | managed Postgres add-on **per service** (Render/Railway both offer this), or one managed instance with multiple databases if the plan allows |
| Redis | one container | managed Redis add-on (Render/Railway both offer this) |
| Kafka | `bitnami/kafka` container | **not offered natively by either platform** — use a managed Kafka service (Confluent Cloud has a free tier, or Upstash Kafka) and point `KAFKA_BOOTSTRAP_SERVERS` at it |
| Service discovery | Eureka container | Eureka still works the same way *between* your own services, as long as they can all reach the Eureka service's public/internal URL |
| Networking | Docker bridge network, services reach each other by container name | Services reach each other by the PaaS-assigned internal hostname — update `EUREKA_HOSTNAME`, `CONFIG_SERVER_HOST`, `DB_HOST`, `KAFKA_BOOTSTRAP_SERVERS`, `REDIS_HOST` env vars per service accordingly |

### Steps (Render, as a representative example)

1. Push this repo to GitHub.
2. For each of the 11 services: **New > Web Service**, point at the repo,
   set the **Root Directory** to the service's folder (e.g. `auth-service`),
   Render auto-detects the `Dockerfile`.
3. Create a managed Postgres instance per data-owning service (auth,
   user, product, inventory, order, payment, notification — 7 total) or
   one instance with 7 databases if your plan supports multiple DBs.
4. Create a managed Redis instance (shared by product-service's cache and
   cart-service's store, or two separate instances for cleaner isolation).
5. Provision a Kafka cluster on Confluent Cloud or Upstash; copy the
   bootstrap-servers connection string.
6. For every service, set the environment variables listed in
   [`.env.example`](../.env.example) plus the service-specific `DB_*`
   values, pointing at the managed instances from steps 3–5 instead of
   the Docker Compose container names.
7. Deploy Eureka Server and Config Server first, note their public Render
   URLs, and set `EUREKA_HOSTNAME` / `CONFIG_SERVER_HOST` on every other
   service to those URLs (not container names, since there's no shared
   Docker network on Render).
8. Deploy the remaining services, gateway last (so it can register the
   already-running services' routes correctly on first boot).

Railway's workflow is nearly identical — a `railway.json` per service or
Railway's "Deploy from Dockerfile" flow, with Railway's built-in Postgres
and Redis plugins substituting for the managed-service steps above.

---

## Production Hardening Checklist

Regardless of which option you pick, before calling this
production-ready:

- [ ] Replace the placeholder `JWT_SECRET` with a securely generated,
      secret-manager-stored value (AWS Secrets Manager / Vault / Render's
      encrypted env vars) — never commit a real secret to the repo.
- [ ] Put TLS termination in front of the gateway (nginx/Caddy reverse
      proxy, or the PaaS's built-in HTTPS).
- [ ] Swap Postgres/Kafka/Redis containers for managed equivalents
      (RDS/Confluent Cloud/ElastiCache, or their DigitalOcean/Render
      equivalents) for anything beyond a demo — self-managed stateful
      containers on a single VM have no automated backups or failover.
- [ ] Tighten `auth-service`'s `/api/auth/internal/**` endpoint (currently
      `permitAll` for service-to-service calls from background Kafka
      listeners — see the comment in `auth-service`'s `SecurityConfig`)
      behind a private subnet, service-mesh mTLS, or an internal API key.
- [ ] Set resource limits (`deploy.resources` in Compose, or the PaaS's
      equivalent) so one runaway service can't starve the others on a
      shared VM.
- [ ] Add centralized logging/tracing (the stack already has clean
      structured logs per service; wiring them into something like
      Loki/CloudWatch/Datadog is the natural next step — Zipkin/
      Prometheus/Grafana were called out as optional in the original
      spec and are a reasonable next addition).
- [ ] Rotate the JWT signing secret on a schedule, and consider moving
      from a shared HMAC secret to per-environment asymmetric (RS256)
      keys if multiple teams/services need to verify tokens without all
      holding the same signing secret.
