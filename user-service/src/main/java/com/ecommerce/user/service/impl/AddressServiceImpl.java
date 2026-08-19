package com.ecommerce.user.service.impl;

import com.ecommerce.user.dto.request.AddressRequest;
import com.ecommerce.user.dto.response.AddressResponse;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.mapper.ProfileMapper;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.service.AddressService;
import com.ecommerce.user.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserProfileService userProfileService;
    private final ProfileMapper profileMapper;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            UserProfileService userProfileService,
            ProfileMapper profileMapper) {
        this.addressRepository = addressRepository;
        this.userProfileService = userProfileService;
        this.profileMapper = profileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(UUID userId) {
        return profileMapper.toAddressResponseList(addressRepository.findByUserProfileId(userId));
    }

    @Override
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        UserProfile profile = userProfileService.getOrCreateProfile(userId);

        Address address = Address.builder()
                .userProfile(profile)
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .addressType(request.addressType())
                .isDefault(request.isDefault())
                .build();

        Address saved = addressRepository.save(address);

        if (saved.isDefault()) {
            addressRepository.clearDefaultForOtherAddresses(userId, saved.getId());
        }

        log.info("Address added for userId={}, addressId={}", userId, saved.getId());
        return profileMapper.toAddressResponse(saved);
    }

    @Override
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserProfileId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setAddressType(request.addressType());
        address.setDefault(request.isDefault());

        Address saved = addressRepository.save(address);

        if (saved.isDefault()) {
            addressRepository.clearDefaultForOtherAddresses(userId, saved.getId());
        }

        log.info("Address updated for userId={}, addressId={}", userId, addressId);
        return profileMapper.toAddressResponse(saved);
    }

    @Override
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findByIdAndUserProfileId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
        addressRepository.delete(address);
        log.info("Address deleted for userId={}, addressId={}", userId, addressId);
    }
}
