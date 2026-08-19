package com.ecommerce.user.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * What the client actually wants when it asks "who am I / show my
 * profile": identity fields fetched live from auth-service via Feign,
 * merged with the extended profile data this service owns.
 */
public record CombinedProfileResponse(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        String phone,
        String avatarUrl,
        LocalDate dateOfBirth,
        String gender,
        String bio,
        List<AddressResponse> addresses,
        LocalDateTime profileUpdatedAt
) {
}
