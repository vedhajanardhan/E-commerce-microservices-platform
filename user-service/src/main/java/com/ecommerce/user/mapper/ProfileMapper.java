package com.ecommerce.user.mapper;

import com.ecommerce.user.dto.response.AddressResponse;
import com.ecommerce.user.dto.response.AuthUserResponse;
import com.ecommerce.user.dto.response.CombinedProfileResponse;
import com.ecommerce.user.dto.response.ProfileResponse;
import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.UserProfile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    AddressResponse toAddressResponse(Address address);

    List<AddressResponse> toAddressResponseList(List<Address> addresses);

    default ProfileResponse toProfileResponse(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        return new ProfileResponse(
                profile.getId(),
                profile.getPhone(),
                profile.getAvatarUrl(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getBio(),
                toAddressResponseList(profile.getAddresses()),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    default CombinedProfileResponse toCombinedProfileResponse(AuthUserResponse authUser, UserProfile profile) {
        return new CombinedProfileResponse(
                authUser.id(),
                authUser.username(),
                authUser.email(),
                authUser.firstName(),
                authUser.lastName(),
                authUser.roles(),
                profile.getPhone(),
                profile.getAvatarUrl(),
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getBio(),
                toAddressResponseList(profile.getAddresses()),
                profile.getUpdatedAt()
        );
    }
}
