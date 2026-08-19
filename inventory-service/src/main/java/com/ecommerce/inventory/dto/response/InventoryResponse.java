package com.ecommerce.inventory.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID productId,
        String sku,
        int quantityAvailable,
        int reorderThreshold,
        boolean lowStock,
        LocalDateTime updatedAt
) {
}
