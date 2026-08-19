package com.ecommerce.cart.client;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductInfo(
        UUID id,
        String sku,
        String name,
        BigDecimal price,
        boolean active
) {
}
