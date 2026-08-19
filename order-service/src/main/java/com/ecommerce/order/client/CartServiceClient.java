package com.ecommerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "CART-SERVICE", path = "/api/cart", configuration = com.ecommerce.order.config.FeignConfig.class)
public interface CartServiceClient {

    @GetMapping
    CartResponse getCart();

    @DeleteMapping
    void clearCart();

    record CartItemResponse(UUID productId, String sku, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
    }

    record CartResponse(UUID userId, List<CartItemResponse> items, int totalItemCount, BigDecimal total, LocalDateTime updatedAt) {
    }
}
