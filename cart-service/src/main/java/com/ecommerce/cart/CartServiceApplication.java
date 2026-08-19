package com.ecommerce.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Shopping Cart Service.
 * <p>
 * Deliberately has no relational database — Redis is the primary store
 * (see {@link com.ecommerce.cart.repository.RedisCartRepository}), not
 * just a cache, since cart data is ephemeral, high-churn, and doesn't
 * need relational integrity. Product details are fetched via OpenFeign
 * from product-service at add-time; stock is checked synchronously
 * against inventory-service before any add/quantity-update succeeds.
 */
@SpringBootApplication
@EnableFeignClients
public class CartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
