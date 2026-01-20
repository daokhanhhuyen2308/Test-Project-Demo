package com.example.demo.service;

import com.example.demo.dto.requests.EmailTaskDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class EmailTaskRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${KEY_EMAIl_REDIS_PREFIX}")
    private static final String KEY_PREFIX = "";


    public EmailTaskRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(EmailTaskDTO emailTaskDTO){
        redisTemplate.opsForValue().set(buildKey(emailTaskDTO.getId()), emailTaskDTO);
    }

    public EmailTaskDTO getByTaskId(String taskId){
        Object obj = redisTemplate.opsForValue().get(buildKey(taskId));
        return obj == null ? null : (EmailTaskDTO) obj;
    }

    public void update(EmailTaskDTO task){
        redisTemplate.opsForValue().set(
                buildKey(task.getId()),
                task,
                Duration.ofHours(24)
        );
    }

    public void delete(String taskId){
        redisTemplate.delete(buildKey(taskId));
    }

    public List<EmailTaskDTO> findAll(){
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys.isEmpty()){
            return Collections.emptyList();
        }

        List<Object> objects = redisTemplate.opsForValue().multiGet(keys);

        if (objects.isEmpty()){
            return Collections.emptyList();
        }

        return objects.stream()
                .filter(Objects::nonNull)
                .map(obj -> (EmailTaskDTO) obj)
                .toList();

    }

    private String buildKey(String taskId) {
        return KEY_PREFIX + taskId;
    }
}
