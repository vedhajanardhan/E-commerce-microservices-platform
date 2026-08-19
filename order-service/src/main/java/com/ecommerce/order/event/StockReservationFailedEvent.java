package com.ecommerce.order.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Consumed from inventory-service's "stock-reservation-failed" topic.
 * This is the compensating-action trigger in the choreographed saga: if
 * the async stock decrement discovers insufficient stock (a race the
 * synchronous pre-check at order placement can't fully close), the order
 * must be walked back to CANCELLED rather than left CONFIRMED with
 * unfulfillable items.
 */
public record StockReservationFailedEvent(
        UUID orderId,
        List<UUID> insufficientProductIds,
        String reason,
        Instant timestamp
) {
}
