package com.ecommerce.order.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published to the "order-created" topic once an order is CONFIRMED
 * (payment succeeded). inventory-service consumes this to decrement
 * stock asynchronously; notification-service consumes it to send an
 * order confirmation. Matches the field shape both of those services'
 * own copies of this contract expect.
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        List<OrderItemEvent> items,
        Instant createdAt
) {
}
