package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.request.AddItemRequest;
import com.ecommerce.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart(UUID userId);

    CartResponse addItem(UUID userId, AddItemRequest request);

    CartResponse updateQuantity(UUID userId, UUID productId, int quantity);

    CartResponse removeItem(UUID userId, UUID productId);

    void clearCart(UUID userId);
}
