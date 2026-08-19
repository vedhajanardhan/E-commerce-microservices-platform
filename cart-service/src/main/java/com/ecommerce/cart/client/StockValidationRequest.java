package com.ecommerce.cart.client;

import java.util.List;
import java.util.UUID;

public record StockValidationRequest(List<StockCheckItem> items) {
    public record StockCheckItem(UUID productId, int quantity) {
    }
}
