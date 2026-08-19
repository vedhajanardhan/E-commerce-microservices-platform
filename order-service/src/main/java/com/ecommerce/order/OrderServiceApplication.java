package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Order Service — the orchestration hub of the checkout flow.
 * <p>
 * Combines synchronous orchestration (Feign calls to cart-service,
 * product-service, inventory-service, and payment-service, all needed
 * before the client can be told whether their order succeeded) with
 * asynchronous choreography (publishes order-created/order-cancelled to
 * Kafka for inventory-service and notification-service; consumes
 * stock-reservation-failed to auto-cancel orders on a post-hoc stock
 * conflict inventory-service's async processing might discover).
 */
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
