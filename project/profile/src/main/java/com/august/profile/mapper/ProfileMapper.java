package com.august.profile.mapper;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.entity.UserProfile;
import com.august.protocol.profile.CreateProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mapping(target = "id", ignore = true)
    UserProfile mapToProfileEntity(CreateProfileRequest request);

    @Mapping(source = "id", target = "profileId")
    ProfileResponse mapToResponse(UserProfile userProfile);
}
