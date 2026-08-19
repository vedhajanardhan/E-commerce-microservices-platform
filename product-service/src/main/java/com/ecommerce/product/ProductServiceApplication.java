package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Product Catalog Service.
 * <p>
 * Owns the products, categories, and product_images tables
 * (database-per-service). Uses Redis for cache-aside reads on the
 * highest-traffic endpoints (single product lookup, category list,
 * search results) — see {@link com.ecommerce.product.config.RedisCacheConfig}.
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
