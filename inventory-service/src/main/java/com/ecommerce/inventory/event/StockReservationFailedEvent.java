package com.ecommerce.inventory.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Published back to order-service (via the "stock-reservation-failed"
 * topic) when one or more items in an OrderCreatedEvent can't be
 * fulfilled. This is the compensating signal in a choreographed saga:
 * order-service is expected to consume this and transition the order to
 * a CANCELLED/FAILED state rather than leaving it stuck as CONFIRMED
 * with unfulfillable stock.
 */
public record StockReservationFailedEvent(
        UUID orderId,
        List<UUID> insufficientProductIds,
        String reason,
        Instant timestamp
) {
}
