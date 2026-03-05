package com.august.profile.service;

import com.august.profile.dto.ProfileResponse;
import com.august.protocol.profile.CreateProfileRequest;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.events.UserRegisteredEvent;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ApiResponse<ProfileResponse> getInfoUserProfile(String profileId);

    ProfileResponse uploadFileAvatar(MultipartFile avatar);

    ProfileResponse createProfile(CreateProfileRequest request);

    void createProfileForUserRegistered(UserRegisteredEvent event);
}
