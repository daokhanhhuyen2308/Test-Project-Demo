package com.august.profile.service;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.dto.UserFollowRequestFilter;
import com.august.profile.dto.UserSummaryResponse;
import com.august.sharecore.dto.PageResponse;

public interface UserFollowService {
    ProfileResponse toggleFollow(String username, String currentUserId);
    void upsertFollow(String followerId, String followingId);
    void deleteByFollowerIdAndFollowingId(String followerId, String followingId);

    PageResponse<UserSummaryResponse> listFollowers(UserFollowRequestFilter filter);

    PageResponse<UserSummaryResponse> listFollowings(UserFollowRequestFilter filter);
}
