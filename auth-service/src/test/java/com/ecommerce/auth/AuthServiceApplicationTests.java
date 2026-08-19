package com.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full Spring context — JPA, Security, JWT beans,
        // MapStruct-generated mapper — wires up against the H2 test DB
        // without needing Eureka or the Config Server to be running.
    }
}
