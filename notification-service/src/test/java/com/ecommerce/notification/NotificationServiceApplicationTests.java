package com.ecommerce.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies JPA, Security, the three per-topic Kafka listener
        // container factories, the Feign client, and the MapStruct-
        // generated mapper all wire up against the H2 test DB without
        // needing Eureka, Config Server, or a live Kafka broker/auth-service.
    }
}
