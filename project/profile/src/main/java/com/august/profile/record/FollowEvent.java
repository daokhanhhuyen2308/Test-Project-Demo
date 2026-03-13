package com.august.profile.record;

import com.august.profile.enums.Action;

public record FollowEvent(String followerId, String followingId, Action action) {
}
