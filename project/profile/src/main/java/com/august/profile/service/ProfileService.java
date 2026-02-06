package com.august.profile.service;

import com.august.profile.dto.ProfileResponse;
import com.august.shared.dto.ApiResponse;

public interface ProfileService {
    ApiResponse<ProfileResponse> getInfoUserProfile(String profileId);
}
