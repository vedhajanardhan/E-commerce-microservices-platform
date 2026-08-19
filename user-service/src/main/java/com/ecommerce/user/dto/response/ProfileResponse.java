package com.ecommerce.user.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String phone,
        String avatarUrl,
        LocalDate dateOfBirth,
        String gender,
        String bio,
        List<AddressResponse> addresses,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
