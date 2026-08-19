package com.ecommerce.user.controller;

import com.ecommerce.user.dto.request.UpdateProfileRequest;
import com.ecommerce.user.dto.response.CombinedProfileResponse;
import com.ecommerce.user.dto.response.ProfileResponse;
import com.ecommerce.user.security.CurrentUserProvider;
import com.ecommerce.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "Manage the authenticated user's profile")
@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final CurrentUserProvider currentUserProvider;

    public UserProfileController(UserProfileService userProfileService, CurrentUserProvider currentUserProvider) {
        this.userProfileService = userProfileService;
        this.currentUserProvider = currentUserProvider;
    }

    @Operation(summary = "Get the current user's full profile (identity + extended profile), merged from auth-service")
    @GetMapping("/me")
    public ResponseEntity<CombinedProfileResponse> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getCombinedProfile(currentUserProvider.getUserId(authentication)));
    }

    @Operation(summary = "Update the current user's extended profile fields (phone, avatar, bio, etc.)")
    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userProfileService.updateProfile(currentUserProvider.getUserId(authentication), request));
    }
}
