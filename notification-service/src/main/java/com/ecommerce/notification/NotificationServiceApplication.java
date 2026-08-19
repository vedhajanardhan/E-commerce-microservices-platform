package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Notification Service — purely event-driven, no synchronous callers.
 * <p>
 * Consumes order-created, payment-processed, and inventory-updated
 * (low-stock only) from Kafka and sends a (mock) email for each. The one
 * outbound call it makes (to auth-service, to resolve a user's email) is
 * itself asynchronous from the caller's perspective — it happens inside
 * a Kafka listener, not in response to any HTTP request.
 */
@SpringBootApplication
@EnableFeignClients
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
