package com.august.post.cronjob;

import com.august.post.repository.jpa.PostRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Component
@RequiredArgsConstructor
public class PostCountSyncJob {
    private final StringRedisTemplate stringRedisTemplate;
    private final PostRepository postRepository;
    @Value("${KEY.COMMENT.COUNT}")
    private String KEY_COMMENT_COUNT;
    @Value("${KEY.VIEW.COUNT}")
    private String KEY_VIEW_COUNT;
    private final ElasticsearchOperations elasticsearchOperations;
    private static final String POST_INDEX = "post_index";
    private static final Logger log = LoggerFactory.getLogger(PostCountSyncJob.class);

    @Scheduled(cron = "0 */1 * * * ?")
    public void syncPostCounts(){
        log.info("Starting sync Redis counters with ES and JPA");
        Map<Long, Long> viewIncMap = new HashMap<>();
        Map<Long, Long> commentIncMap = new HashMap<>();

        scanRedis(KEY_VIEW_COUNT, (postId, inc) -> viewIncMap.merge(postId, inc, Long::sum));
        scanRedis(KEY_COMMENT_COUNT, (postId, inc) -> commentIncMap.merge(postId, inc, Long::sum));

        Set<Long> postIdsToUpdate = new HashSet<>();
        postIdsToUpdate.addAll(viewIncMap.keySet());
        postIdsToUpdate.addAll(commentIncMap.keySet());

        postIdsToUpdate.parallelStream().forEach(postId -> {
                        Long viewInc = viewIncMap.getOrDefault(postId, 0L);
                        Long commentInc = commentIncMap.getOrDefault(postId, 0L);
                        postRepository.updateCounts(postId, viewInc, commentInc);
                        updateBothInES(postId, viewInc, commentInc);
                });
    }

    private void scanRedis(String keyPrefix, BiConsumer<Long, Long> jpaUpdater) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(keyPrefix + "_*")
                .count(1000)
                .build();

        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {

            while (cursor.hasNext()) {
                String key = cursor.next();

                String postId = key.substring((keyPrefix + "_").length());
                String deltaCount = stringRedisTemplate.opsForValue().get(key);

                if (deltaCount == null) continue;

                jpaUpdater.accept(Long.parseLong(deltaCount), Long.parseLong(postId));

                stringRedisTemplate.delete(key);
            }

        } catch (Exception e) {
            log.error("Scan error for prefix {}: {}", keyPrefix, e.getMessage());
        }
    }

    private void updateBothInES(Long postId, Long viewInc, Long commentInc){
        if (viewInc == 0 && commentInc == 0) return;

        String script = """
                ctx._source.viewCount += param.viewInc;
                ctx._source.commentCount += param.commentInc;
                """;

        Map<String, Object> params = Map.of("viewInc", viewInc, "commentInc", commentInc);

        UpdateQuery updateQuery = UpdateQuery.builder(postId.toString())
                .withScript(script)
                .withScriptType(ScriptType.INLINE)
                .withLang("painless")
                .withParams(params)
                .build();

        try{
            elasticsearchOperations.update(updateQuery, IndexCoordinates.of(POST_INDEX));
        } catch (Exception e) {
            log.error("ES update failed for post {} (view +{}, comment +{}): {}", postId, viewInc,
                    commentInc, e.getMessage());
        }
    }

}
