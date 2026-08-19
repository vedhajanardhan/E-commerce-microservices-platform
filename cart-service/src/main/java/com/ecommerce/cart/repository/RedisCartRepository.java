package com.ecommerce.cart.repository;

import com.ecommerce.cart.model.Cart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis IS the database for this service — there's no Postgres behind
 * it. Carts are ephemeral, read/written far more often than most
 * platform data, and don't need relational integrity or joins, so a
 * key-value document store keyed by userId is a better fit than a
 * cart/cart_items table pair. A TTL means abandoned carts simply expire
 * instead of needing a scheduled cleanup job.
 */
@Repository
public class RedisCartRepository implements CartRepository {

    private static final String KEY_PREFIX = "cart:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration cartTtl;

    public RedisCartRepository(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${cart.ttl-days:30}") long ttlDays) {
        this.redisTemplate = redisTemplate;
        this.cartTtl = Duration.ofDays(ttlDays);
    }

    @Override
    public Optional<Cart> findByUserId(UUID userId) {
        Object value = redisTemplate.opsForValue().get(key(userId));
        return Optional.ofNullable((Cart) value);
    }

    @Override
    public void save(Cart cart) {
        redisTemplate.opsForValue().set(key(cart.getUserId()), cart, cartTtl);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(UUID userId) {
        return KEY_PREFIX + userId;
    }
}
