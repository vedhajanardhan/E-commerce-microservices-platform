package com.ecommerce.inventory.dto.response;

import com.ecommerce.inventory.entity.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID productId,
        MovementType movementType,
        int quantity,
        int resultingQuantity,
        String reason,
        UUID orderId,
        LocalDateTime createdAt
) {
}
