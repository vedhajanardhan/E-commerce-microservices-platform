package com.ecommerce.user.client;

import com.ecommerce.user.dto.response.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Talks to auth-service by its Eureka service-id ("AUTH-SERVICE") rather
 * than a hardcoded host:port — Eureka + Ribbon/Spring Cloud LoadBalancer
 * resolve the actual instance address at call time.
 */
@FeignClient(name = "AUTH-SERVICE", path = "/api/auth", configuration = com.ecommerce.user.config.FeignConfig.class)
public interface AuthServiceClient {

    @GetMapping("/internal/users/{userId}")
    AuthUserResponse getUserById(@PathVariable("userId") UUID userId);
}
