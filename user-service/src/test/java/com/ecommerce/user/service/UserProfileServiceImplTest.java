package com.ecommerce.user.service;

import com.ecommerce.user.client.AuthServiceClient;
import com.ecommerce.user.dto.request.UpdateProfileRequest;
import com.ecommerce.user.dto.response.AuthUserResponse;
import com.ecommerce.user.dto.response.CombinedProfileResponse;
import com.ecommerce.user.dto.response.ProfileResponse;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.mapper.ProfileMapper;
import com.ecommerce.user.repository.UserProfileRepository;
import com.ecommerce.user.service.impl.UserProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private AuthServiceClient authServiceClient;
    @Mock private ProfileMapper profileMapper;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getOrCreateProfile_whenProfileExists_returnsExisting() {
        UserProfile existing = UserProfile.builder().id(userId).phone("1234567890").build();
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existing));

        UserProfile result = userProfileService.getOrCreateProfile(userId);

        assertEquals("1234567890", result.getPhone());
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void getOrCreateProfile_whenNoProfile_createsEmptyOne() {
        when(userProfileRepository.findById(userId)).thenReturn(Optional.empty());
        UserProfile created = UserProfile.builder().id(userId).build();
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(created);

        UserProfile result = userProfileService.getOrCreateProfile(userId);

        assertEquals(userId, result.getId());
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_onlyUpdatesNonNullFields() {
        UserProfile existing = UserProfile.builder()
                .id(userId)
                .phone("1111111111")
                .bio("old bio")
                .build();
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null, null, "new bio");
        ProfileResponse expected = new ProfileResponse(userId, "1111111111", null, null, null, "new bio",
                List.of(), LocalDateTime.now(), LocalDateTime.now());
        when(profileMapper.toProfileResponse(any(UserProfile.class))).thenReturn(expected);

        ProfileResponse response = userProfileService.updateProfile(userId, request);

        assertEquals("new bio", response.bio());
        assertEquals("1111111111", existing.getPhone()); // untouched since request.phone() was null
    }

    @Test
    void getCombinedProfile_mergesAuthDataAndLocalProfile() {
        AuthUserResponse authUser = new AuthUserResponse(
                userId, "vedha", "vedha@example.com", "Vedha", "J", Set.of("ROLE_USER"), true, LocalDateTime.now());
        UserProfile profile = UserProfile.builder().id(userId).phone("9999999999").build();

        when(authServiceClient.getUserById(userId)).thenReturn(authUser);
        when(userProfileRepository.findById(userId)).thenReturn(Optional.of(profile));

        CombinedProfileResponse expected = new CombinedProfileResponse(
                userId, "vedha", "vedha@example.com", "Vedha", "J", Set.of("ROLE_USER"),
                "9999999999", null, (LocalDate) null, null, null, List.of(), LocalDateTime.now());
        when(profileMapper.toCombinedProfileResponse(authUser, profile)).thenReturn(expected);

        CombinedProfileResponse result = userProfileService.getCombinedProfile(userId);

        assertEquals("vedha", result.username());
        assertEquals("9999999999", result.phone());
        verify(authServiceClient).getUserById(userId);
    }
}
