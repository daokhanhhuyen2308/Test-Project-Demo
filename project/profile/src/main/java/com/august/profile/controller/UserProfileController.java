package com.august.profile.controller;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.service.UserFollowService;
import com.august.profile.service.UserProfileService;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharesecurity.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserFollowService userFollowService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{profileId}")
    public ApiResponse<ProfileResponse> getInfoUserProfile(@PathVariable String profileId){
        return userProfileService.getInfoUserProfile(profileId);
    }

    @PostMapping(value = "/me/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProfileResponse> uploadAvatar(HttpServletRequest request,
                                                     @RequestParam("avatar") MultipartFile avatar){
        System.out.println("Content Type: " + request.getContentType());
        return ApiResponse.success(userProfileService.uploadFileAvatar(avatar), "Upload avatar successfully!");
    }

    @PostMapping("/{username}/follow")
    public ApiResponse<ProfileResponse> toggleFollow(@PathVariable String username) {

        String currentUserId = securityUtils.getCurrentUser().getKeycloakId();

        ProfileResponse profile = userFollowService.toggleFollow(username, currentUserId);

        return ApiResponse.<ProfileResponse>builder()
                .result(profile)
                .message(profile.getIsFollowing() ? "Followed successfully" : "Unfollowed successfully")
                .build();
    }


}
