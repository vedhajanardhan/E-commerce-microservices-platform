package com.ecommerce.cart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CartServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Security, the RedisTemplate bean, Feign clients, and
        // MapStruct-generated mapper all wire up without needing Eureka,
        // Config Server, or live Redis/product-service/inventory-service
        // connections (Lettuce's connection factory and Feign proxies
        // are both lazy, so absence of those services isn't fatal here).
    }
}
