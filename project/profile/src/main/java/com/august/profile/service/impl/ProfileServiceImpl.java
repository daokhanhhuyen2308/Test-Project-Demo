package com.august.profile.service.impl;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.entity.UserProfile;
import com.august.profile.mapper.ProfileMapper;
import com.august.profile.repository.ProfileRepository;
import com.august.profile.service.ProfileService;
import com.august.shared.dto.ApiResponse;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    @Override
    public ApiResponse<ProfileResponse> getInfoUserProfile(String profileId) {
        UserProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppCustomException(ErrorCode.USER_NOT_FOUND));

        ApiResponse<ProfileResponse> response = new ApiResponse<>();
        response.setResult(profileMapper.mapToResponse(profile));
        return response;
    }
}
