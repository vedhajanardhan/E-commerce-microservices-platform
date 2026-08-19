package com.ecommerce.cart.client;

import java.util.List;
import java.util.UUID;

public record StockValidationResponse(boolean valid, List<UUID> insufficientProductIds) {
}
