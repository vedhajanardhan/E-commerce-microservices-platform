package com.ecommerce.user.service.impl;

import com.ecommerce.user.client.AuthServiceClient;
import com.ecommerce.user.dto.request.UpdateProfileRequest;
import com.ecommerce.user.dto.response.AuthUserResponse;
import com.ecommerce.user.dto.response.CombinedProfileResponse;
import com.ecommerce.user.dto.response.ProfileResponse;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.exception.AuthServiceUnavailableException;
import com.ecommerce.user.mapper.ProfileMapper;
import com.ecommerce.user.repository.UserProfileRepository;
import com.ecommerce.user.service.UserProfileService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AuthServiceClient authServiceClient;
    private final ProfileMapper profileMapper;

    public UserProfileServiceImpl(
            UserProfileRepository userProfileRepository,
            AuthServiceClient authServiceClient,
            ProfileMapper profileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.authServiceClient = authServiceClient;
        this.profileMapper = profileMapper;
    }

    @Override
    public UserProfile getOrCreateProfile(UUID userId) {
        return userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    log.info("No profile found for userId={}, creating an empty one", userId);
                    UserProfile profile = UserProfile.builder().id(userId).build();
                    return userProfileRepository.save(profile);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        return profileMapper.toProfileResponse(getOrCreateProfile(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public CombinedProfileResponse getCombinedProfile(UUID userId) {
        AuthUserResponse authUser = fetchAuthUser(userId);
        UserProfile profile = getOrCreateProfile(userId);
        return profileMapper.toCombinedProfileResponse(authUser, profile);
    }

    @Override
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = getOrCreateProfile(userId);

        if (request.phone() != null) profile.setPhone(request.phone());
        if (request.avatarUrl() != null) profile.setAvatarUrl(request.avatarUrl());
        if (request.dateOfBirth() != null) profile.setDateOfBirth(request.dateOfBirth());
        if (request.gender() != null) profile.setGender(request.gender());
        if (request.bio() != null) profile.setBio(request.bio());

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Profile updated for userId={}", userId);
        return profileMapper.toProfileResponse(saved);
    }

    private AuthUserResponse fetchAuthUser(UUID userId) {
        try {
            return authServiceClient.getUserById(userId);
        } catch (FeignException e) {
            log.error("Failed to fetch user {} from auth-service: status={}", userId, e.status(), e);
            throw new AuthServiceUnavailableException("Unable to retrieve identity information", e);
        }
    }
}
