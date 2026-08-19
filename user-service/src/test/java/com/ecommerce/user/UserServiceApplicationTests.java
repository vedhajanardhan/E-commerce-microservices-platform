package com.ecommerce.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies JPA, Security, Feign client, and MapStruct-generated
        // mapper beans all wire up against the H2 test DB without
        // needing Eureka, Config Server, or auth-service to be running.
    }
}
