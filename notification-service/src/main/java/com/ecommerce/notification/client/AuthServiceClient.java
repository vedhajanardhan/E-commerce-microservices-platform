package com.ecommerce.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Unlike every other Feign client in this platform, this one is called
 * from a background Kafka listener thread — there's no incoming HTTP
 * request, so there's no user JWT to forward. It hits auth-service's
 * permitAll internal endpoint directly.
 */
@FeignClient(name = "AUTH-SERVICE", path = "/api/auth/internal")
public interface AuthServiceClient {

    @GetMapping("/users/{userId}")
    AuthUserInfo getUserById(@PathVariable("userId") UUID userId);
}
