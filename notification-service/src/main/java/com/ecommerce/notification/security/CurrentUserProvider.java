package com.ecommerce.notification.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
