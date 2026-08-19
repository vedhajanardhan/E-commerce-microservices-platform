package com.ecommerce.notification.client;

import java.util.UUID;

public record AuthUserInfo(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName
) {
}
