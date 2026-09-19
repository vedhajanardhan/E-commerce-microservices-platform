# E-Commerce Microservices Platform

A backend-only, distributed e-commerce system built to practice production-style microservices patterns: service discovery, centralized config, JWT auth at the gateway, circuit breakers, and database-per-service isolation.

## Live Demo
- Eureka Service Registry (shows all deployed services registered and UP): https://ecommerce-microservices-platform-tlzi.onrender.com
- API Gateway: https://ecommerce-api-gateway-0cfj.onrender.com

## Architecture

11 independently deployable Spring Boot services:

| Service | Responsibility |
|---|---|
| eureka-server | Service discovery registry |
| config-server | Centralized configuration for all services |
| api-gateway | Single entry point; JWT validation, routing, circuit breakers |
| auth-service | User registration, login, JWT issuance |
| user-service | User profile management |
| product-service | Product catalog, search, pagination |
| cart-service | Shopping cart |
| order-service | Order placement and history |
| payment-service | Payment processing (mock gateway) |
| inventory-service | Stock tracking |
| notification-service | Order/user notifications |

## Tech Stack
- **Framework:** Spring Boot 3, Spring Cloud (Eureka, Config Server, Gateway)
- **Auth:** Stateless JWT, validated at the API Gateway before requests reach downstream services
- **Database:** PostgreSQL, one schema per service, Flyway-managed migrations
- **Resilience:** Resilience4j circuit breakers with fallback responses per downstream service
- **Deployment:** Each service deployed independently on Render; PostgreSQL hosted on Aiven
- **API Docs:** springdoc-openapi (Swagger UI) per service
- **Testing:** Postman collection included in `/postman`

## Local Development

```bash
docker-compose up
```

This spins up all services plus local MySQL/PostgreSQL, Redis, and Kafka for full local testing. See `.env.example` for required environment variables.

## Notes on the Deployed Demo

The free-tier deployment differs slightly from the local docker-compose setup:
- Free-tier services sleep after 15 minutes of inactivity; the first request after sleep may take 30-50 seconds to respond
- Redis caching in product-service is disabled in this deployment (works locally via docker-compose) — the free-tier Redis connection needs further tuning
- All services connect to a single shared PostgreSQL instance (Aiven free tier), partitioned by database, rather than one database server per service as in local development

## API Documentation

Each service exposes Swagger UI at `/swagger-ui.html` on its own port/URL. A full Postman collection covering all endpoints is available in `/postman`.
