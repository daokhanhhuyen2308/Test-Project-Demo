package com.august.profile.controller;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.service.ProfileService;
import com.august.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{profileId}")
    public ApiResponse<ProfileResponse> getInfoUserProfile(@PathVariable String profileId){
        return profileService.getInfoUserProfile(profileId);
    }


}
