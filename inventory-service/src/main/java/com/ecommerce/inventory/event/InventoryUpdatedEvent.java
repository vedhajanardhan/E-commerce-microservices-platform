package com.ecommerce.inventory.event;

import com.ecommerce.inventory.entity.MovementType;

import java.time.Instant;
import java.util.UUID;

public record InventoryUpdatedEvent(
        UUID productId,
        MovementType movementType,
        int quantityChanged,
        int quantityRemaining,
        boolean lowStock,
        Instant timestamp
) {
}
