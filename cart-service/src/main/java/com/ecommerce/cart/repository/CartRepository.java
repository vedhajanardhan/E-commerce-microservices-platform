package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Optional<Cart> findByUserId(UUID userId);

    void save(Cart cart);

    void deleteByUserId(UUID userId);
}
