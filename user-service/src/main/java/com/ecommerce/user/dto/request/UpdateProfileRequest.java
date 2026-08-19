package com.ecommerce.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be a valid number (7-15 digits, optional leading +)")
        String phone,

        @Size(max = 512)
        String avatarUrl,

        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,

        @Size(max = 20)
        String gender,

        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio
) {
}
