package com.ecommerce.inventory.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record StockValidationRequest(

        @NotEmpty(message = "At least one item is required")
        List<@Valid StockCheckItem> items
) {
    public record StockCheckItem(
            @NotNull UUID productId,
            @Min(value = 1, message = "Quantity must be at least 1") int quantity
    ) {
    }
}
