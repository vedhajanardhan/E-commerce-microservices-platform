package com.ecommerce.inventory.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors order-service's "order-cancelled" event. Consumed here to
 * restock items that were previously decremented when the order was
 * confirmed — order-service only ever publishes this for orders that
 * were actually CONFIRMED (and therefore already decremented), so
 * every item in the list is safe to add back.
 */
public record OrderCancelledEvent(
        UUID orderId,
        List<OrderItemEvent> items,
        String reason,
        Instant timestamp
) {
}
