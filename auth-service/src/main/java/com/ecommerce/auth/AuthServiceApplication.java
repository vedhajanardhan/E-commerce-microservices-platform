package com.ecommerce.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Authentication & Authorization Service.
 * <p>
 * Owns the users, roles, and refresh_tokens tables (database-per-service).
 * Issues stateless JWT access tokens (validated by every other service and
 * pre-checked by the gateway) and persisted, rotatable refresh tokens.
 */
@SpringBootApplication
@EnableFeignClients
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
