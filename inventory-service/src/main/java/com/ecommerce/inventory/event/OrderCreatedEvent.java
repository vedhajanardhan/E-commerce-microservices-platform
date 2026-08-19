package com.ecommerce.inventory.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Mirrors the event order-service publishes to the "order-created" topic
 * after an order is successfully placed. Kept as inventory-service's own
 * copy of the contract (not a shared library) — each service owns its
 * own view of events it consumes, so a field order-service adds for its
 * own purposes doesn't force a lockstep redeploy here.
 */
public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        List<OrderItemEvent> items,
        Instant createdAt
) {
}
