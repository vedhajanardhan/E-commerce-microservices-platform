package com.ecommerce.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies JPA, Security, Kafka listener container, and
        // MapStruct-generated mapper all wire up against the H2 test DB
        // without needing Eureka, Config Server, or a live Kafka broker
        // (listener container starts but connection failures are
        // retried in the background, not fatal to context startup).
    }
}
