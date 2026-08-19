package com.ecommerce.inventory.dto.response;

import java.util.List;
import java.util.UUID;

public record StockValidationResponse(
        boolean valid,
        List<UUID> insufficientProductIds
) {

    public static StockValidationResponse success() {
        return new StockValidationResponse(true, List.of());
    }

    public static StockValidationResponse invalid(List<UUID> insufficientProductIds) {
        return new StockValidationResponse(false, insufficientProductIds);
    }
}
