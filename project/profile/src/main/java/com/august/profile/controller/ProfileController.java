package com.august.profile.controller;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.service.ProfileService;
import com.august.sharecore.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{profileId}")
    public ApiResponse<ProfileResponse> getInfoUserProfile(@PathVariable String profileId){
        return profileService.getInfoUserProfile(profileId);
    }

    @PostMapping(value = "/me/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProfileResponse> uploadAvatar(HttpServletRequest request, @RequestParam("avatar") MultipartFile avatar){
        System.out.println("Content Type: " + request.getContentType());
        return ApiResponse.<ProfileResponse>builder()
                .result(profileService.uploadFileAvatar(avatar))
                .build();
    }


}
