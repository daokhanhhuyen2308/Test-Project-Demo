package com.august.post.cronjob;

import com.august.post.record.PostCountCommentPair;
import com.august.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
@RequiredArgsConstructor
public class PostCommentCountSyncJob {
    private final RedisTemplate<String, Object> redisTemplate;
    private final PostRepository postRepository;
    @Value("${KEY.COMMENT.COUNT}")
    private String KEY_COMMENT_COUNT;

    @Scheduled(fixedDelay = 300000)
    public void syncCommentCountFromRedis(){
        redisTemplate.keys(KEY_COMMENT_COUNT +"*")
                .stream()
                .map(key -> key.split(":")[2])
                .map(postId -> {
                    Long count = (Long) redisTemplate.opsForValue().get(KEY_COMMENT_COUNT + postId);
                    return count != null ? new PostCountCommentPair(count, Long.parseLong(postId)) : null;
                })
                .filter(Objects::nonNull)
                .forEach(pair -> postRepository.updateCommentCount(pair.count(), pair.postId()));

    }
}
