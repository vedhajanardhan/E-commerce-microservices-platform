package com.ecommerce.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the native-profile config repo loads and the Spring
        // context wires up without errors.
    }
}
