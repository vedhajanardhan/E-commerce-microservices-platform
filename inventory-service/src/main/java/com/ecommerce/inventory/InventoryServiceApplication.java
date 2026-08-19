package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Inventory Service.
 * <p>
 * Owns the inventory_items, stock_movements, and processed_order_events
 * tables (database-per-service). Stock validation at checkout is
 * synchronous (called via OpenFeign/REST from cart/order-service); the
 * actual decrement happens asynchronously by consuming order-created
 * events from Kafka (see {@link com.ecommerce.inventory.kafka.OrderEventListener}),
 * with idempotent processing to tolerate Kafka's at-least-once delivery.
 */
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
