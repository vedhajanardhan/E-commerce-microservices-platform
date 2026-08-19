package com.ecommerce.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service — a dummy payment gateway.
 * <p>
 * Called synchronously (via OpenFeign) by order-service during checkout,
 * since the shopper needs an immediate success/failure result. Publishes
 * a "payment-processed" Kafka event afterward for notification-service
 * to send a payment confirmation/failure email, decoupled from the
 * synchronous request/response path.
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
