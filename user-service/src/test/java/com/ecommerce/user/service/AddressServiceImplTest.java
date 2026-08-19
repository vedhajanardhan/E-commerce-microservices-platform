package com.ecommerce.user.service;

import com.ecommerce.user.dto.request.AddressRequest;
import com.ecommerce.user.dto.response.AddressResponse;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.AddressType;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.mapper.ProfileMapper;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserProfileService userProfileService;
    @Mock private ProfileMapper profileMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private UUID userId;
    private AddressRequest request;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        request = new AddressRequest("221B Baker St", null, "Bengaluru", "Karnataka",
                "560001", "India", AddressType.SHIPPING, true);
    }

    @Test
    void addAddress_persistsAndClearsOtherDefaults() {
        UserProfile profile = UserProfile.builder().id(userId).build();
        when(userProfileService.getOrCreateProfile(userId)).thenReturn(profile);

        Address saved = Address.builder()
                .id(UUID.randomUUID())
                .userProfile(profile)
                .addressLine1("221B Baker St")
                .city("Bengaluru")
                .isDefault(true)
                .build();
        when(addressRepository.save(any(Address.class))).thenReturn(saved);
        when(profileMapper.toAddressResponse(saved)).thenReturn(
                new AddressResponse(saved.getId(), "221B Baker St", null, "Bengaluru", "Karnataka",
                        "560001", "India", AddressType.SHIPPING, true));

        AddressResponse response = addressService.addAddress(userId, request);

        assertTrue(response.isDefault());
        verify(addressRepository).clearDefaultForOtherAddresses(userId, saved.getId());
    }

    @Test
    void updateAddress_whenAddressNotFound_throwsResourceNotFoundException() {
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findByIdAndUserProfileId(addressId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.updateAddress(userId, addressId, request));
    }

    @Test
    void deleteAddress_whenOwnedByUser_deletesSuccessfully() {
        UUID addressId = UUID.randomUUID();
        Address address = Address.builder().id(addressId).build();
        when(addressRepository.findByIdAndUserProfileId(addressId, userId)).thenReturn(Optional.of(address));

        addressService.deleteAddress(userId, addressId);

        verify(addressRepository).delete(address);
    }

    @Test
    void deleteAddress_whenNotOwnedByUser_throwsResourceNotFoundException() {
        UUID addressId = UUID.randomUUID();
        when(addressRepository.findByIdAndUserProfileId(addressId, userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(userId, addressId));
        verify(addressRepository, never()).delete(any());
    }
}
