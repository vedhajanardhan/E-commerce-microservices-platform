package com.ecommerce.product.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        String categoryName,
        String brand,
        boolean active,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) implements Serializable {
}
