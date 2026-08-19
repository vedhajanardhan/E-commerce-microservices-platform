package com.ecommerce.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies JPA, Security, the Kafka producer, and the
        // MapStruct-generated mapper all wire up against the H2 test DB
        // without needing Eureka, Config Server, or a live Kafka broker.
    }
}
