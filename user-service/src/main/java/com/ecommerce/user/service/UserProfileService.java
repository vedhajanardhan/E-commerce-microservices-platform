package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.UpdateProfileRequest;
import com.ecommerce.user.dto.response.CombinedProfileResponse;
import com.ecommerce.user.dto.response.ProfileResponse;
import com.ecommerce.user.entity.UserProfile;

import java.util.UUID;

public interface UserProfileService {

    /** Fetches the local profile, lazily creating an empty one if this is the user's first request. */
    UserProfile getOrCreateProfile(UUID userId);

    ProfileResponse getProfile(UUID userId);

    CombinedProfileResponse getCombinedProfile(UUID userId);

    ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
