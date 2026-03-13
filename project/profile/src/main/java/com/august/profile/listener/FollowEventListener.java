package com.august.profile.listener;

import com.august.profile.enums.Action;
import com.august.profile.record.FollowEvent;
import com.august.profile.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowEventListener {
    private final UserFollowService userFollowService;

    @EventListener
    @Async
    public void handleFollowEvent(FollowEvent event) {
        if (Action.FOLLOW.equals(event.action())) {
            userFollowService.upsertFollow(event.followerId(), event.followingId());
        } else {
            userFollowService.deleteByFollowerIdAndFollowingId(event.followerId(), event.followingId());
        }
    }

}
