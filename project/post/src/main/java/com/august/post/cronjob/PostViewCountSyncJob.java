package com.august.post.cronjob;

import com.august.post.record.PostViewCountPair;
import com.august.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PostViewCountSyncJob {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostRepository postRepository;
    @Value("${KEY.VIEW.COUNT}")
    private String KEY_VIEW_COUNT;

    @Scheduled(fixedDelay = 60000)
    public void syncViewCountFromRedis(){
        redisTemplate.keys(KEY_VIEW_COUNT +"*")
                .stream()
                .map(key -> key.split(":")[2])
                .map(postId -> {
                    Long count = (Long) redisTemplate.opsForValue().get(KEY_VIEW_COUNT + postId);
                    return count != null ? new PostViewCountPair(count, Long.parseLong(postId)) : null;
                })
                .filter(Objects::nonNull)
                .forEach(pair -> postRepository.updateViewCount(pair.count(), pair.postId()));

    }
}
