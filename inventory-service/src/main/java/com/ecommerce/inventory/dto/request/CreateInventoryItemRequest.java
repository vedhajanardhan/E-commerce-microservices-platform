package com.ecommerce.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInventoryItemRequest(

        @NotNull(message = "Product id is required")
        UUID productId,

        @NotBlank(message = "SKU is required")
        String sku,

        @NotNull(message = "Initial quantity is required")
        @Min(value = 0, message = "Initial quantity cannot be negative")
        Integer initialQuantity,

        @Min(value = 0, message = "Reorder threshold cannot be negative")
        Integer reorderThreshold
) {
}
