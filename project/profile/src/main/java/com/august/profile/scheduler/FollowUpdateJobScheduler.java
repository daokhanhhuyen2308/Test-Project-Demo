package com.august.profile.scheduler;

import com.august.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowUpdateJobScheduler {
    private final StringRedisTemplate redisTemplate;
    private final UserProfileService userProfileService;
    private static final String FOLLOWER_KEY = "user:followers:*";
    private static final String FOLLOWING_KEY = "user:followings:*";

    @Scheduled(fixedDelay = 300000)
    public void syncFollowCounts(){
        syncData(FOLLOWER_KEY, true);
        syncData(FOLLOWING_KEY, false);

    }

    private void syncData(String keyPattern, boolean isFollower){
        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(keyPattern)
                .count(100)
                .build();

            try (Cursor<String> cursor = redisTemplate.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    String keycloakId = extractIdFromKey(key);

                    Long count = redisTemplate.opsForSet().size(key);

                    if (count != null && keycloakId != null) {
                        if (isFollower) {
                            userProfileService.updateFollowerCount(keycloakId, count);
                        } else {
                            userProfileService.updateFollowingCount(keycloakId, count);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error when syncing pattern {}:", keyPattern, e);
            }
    }

    private String extractIdFromKey(String key) {
        String[] parts = key.split(":");
        return (parts.length >= 3) ? parts[2] : null;
    }

}
