package com.ecommerce.user.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * auth-service's /api/auth/internal/users/{id} endpoint requires
 * authentication (anyRequest().authenticated()), so the incoming
 * request's bearer token is forwarded on every outgoing Feign call.
 * Without this, service-to-service calls would be rejected with 401.
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor authorizationHeaderForwardingInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }
            }
        };
    }
}
