package com.ecommerce.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Mirrors auth-service's UserResponse shape. Kept as a separate,
 * intentionally duplicated DTO (rather than a shared library) so
 * user-service isn't compile-time coupled to auth-service's internal
 * contracts — each service owns its own view of "what a user looks
 * like from here."
 */
public record AuthUserResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean enabled,
        LocalDateTime createdAt
) {
}
