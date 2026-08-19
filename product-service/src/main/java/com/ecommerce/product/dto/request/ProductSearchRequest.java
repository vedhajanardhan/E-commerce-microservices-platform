package com.ecommerce.product.dto.request;

import java.math.BigDecimal;

public record ProductSearchRequest(
        String keyword,
        Long categoryId,
        String brand,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
