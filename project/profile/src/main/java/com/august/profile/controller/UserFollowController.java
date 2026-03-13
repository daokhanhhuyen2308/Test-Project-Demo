package com.august.profile.controller;

import com.august.profile.dto.UserFollowRequestFilter;
import com.august.profile.dto.UserSummaryResponse;
import com.august.profile.service.UserFollowService;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.dto.PageResponse;
import com.august.sharesecurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final SecurityUtils securityUtils;

    @GetMapping("/{authorId}/followers")
    public ApiResponse<PageResponse<UserSummaryResponse>> getListFollowers(@PathVariable String authorId,
                                                                           @ModelAttribute UserFollowRequestFilter
                                                                                       filter){
        String currentKeycloakId = securityUtils.getCurrentUser().getKeycloakId();

        filter.setCurrentKeycloakId(currentKeycloakId);
        filter.setAuthorId(authorId);

        return ApiResponse.success(userFollowService.listFollowers(filter),
                "Retrieve successfully list followers of the author");
    }

    @GetMapping("/{authorId}/followings")
    public ApiResponse<PageResponse<UserSummaryResponse>> getListFollowings(@PathVariable String authorId,
                                                              @ModelAttribute UserFollowRequestFilter filter){
        String currentKeycloakId = securityUtils.getCurrentUser().getKeycloakId();
        filter.setAuthorId(authorId);
        filter.setCurrentKeycloakId(currentKeycloakId);

        return ApiResponse.success(userFollowService.listFollowings(filter),
                "Retrieve successfully list followings of the author");
    }
}
