package com.ecommerce.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies JPA, Security, Feign clients (Cart/Product/Inventory/
        // Payment), the Kafka listener/producer, and the MapStruct-
        // generated mapper all wire up against the H2 test DB without
        // needing Eureka, Config Server, or any live downstream service.
    }
}
