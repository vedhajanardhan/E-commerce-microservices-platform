package com.ecommerce.product.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 50)
        String sku,

        @NotBlank(message = "Product name is required")
        @Size(max = 200)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Price may have at most 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Category id is required")
        Long categoryId,

        @Size(max = 100)
        String brand,

        Boolean active,

        @Size(max = 10, message = "A product may have at most 10 images")
        List<@NotBlank @Size(max = 1024) String> imageUrls
) {
}
