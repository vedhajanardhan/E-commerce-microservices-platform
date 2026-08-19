package com.ecommerce.notification.event;

import java.time.Instant;
import java.util.UUID;

public record InventoryUpdatedEvent(
        UUID productId,
        String movementType,
        int quantityChanged,
        int quantityRemaining,
        boolean lowStock,
        Instant timestamp
) {
}
