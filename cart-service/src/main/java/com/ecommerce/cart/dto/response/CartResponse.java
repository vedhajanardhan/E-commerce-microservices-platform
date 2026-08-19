package com.ecommerce.cart.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID userId,
        List<CartItemResponse> items,
        int totalItemCount,
        BigDecimal total,
        LocalDateTime updatedAt
) {
}
