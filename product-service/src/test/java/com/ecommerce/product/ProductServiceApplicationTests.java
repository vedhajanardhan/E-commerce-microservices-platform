package com.ecommerce.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies JPA, Security, the in-memory CacheManager (test
        // profile), and MapStruct-generated mapper all wire up against
        // the H2 test DB without needing Eureka, Config Server, or a
        // live Redis instance.
    }
}
