package com.ecommerce.product.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        String slug,
        Long parentId,
        String parentName,
        LocalDateTime createdAt
) implements Serializable {
}
