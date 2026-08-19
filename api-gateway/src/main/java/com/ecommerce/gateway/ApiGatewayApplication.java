package com.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway — the single entry point for all client traffic.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Routes requests to the correct downstream service via Eureka
 *       service discovery (lb://SERVICE-ID).</li>
 *   <li>Performs a first-pass JWT validation in {@link com.ecommerce.gateway.filter.JwtAuthenticationFilter}
 *       so malformed/expired/missing tokens are rejected at the edge,
 *       before hitting any downstream service.</li>
 *   <li>Forwards the authenticated user's id and roles to downstream
 *       services via headers (X-User-Id, X-User-Roles) so those services
 *       can trust the identity without re-parsing the JWT themselves.</li>
 *   <li>Applies per-route circuit breakers so a slow/failing downstream
 *       service degrades gracefully instead of cascading failures.</li>
 * </ul>
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
