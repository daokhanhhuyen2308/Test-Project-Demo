package com.august.file.service.impl;

import com.august.file.dto.requests.EmailTaskDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class EmailTaskRedisService {

    private final RedisTemplate<String, EmailTaskDTO> redisTemplate;

    @Value("${KEY_EMAIL_REDIS_PREFIX}")
    private String KEY_PREFIX;

    public EmailTaskRedisService(RedisTemplate<String, EmailTaskDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(EmailTaskDTO emailTaskDTO){
        redisTemplate.opsForValue().set(buildKey(emailTaskDTO.getId()), emailTaskDTO);
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

    public List<EmailTaskDTO> findAll() {
        String pattern = KEY_PREFIX + "*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null){
            return new ArrayList<>();
        }
        return redisTemplate.opsForValue()
                .multiGet(keys).stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String buildKey(String taskId) {
        return KEY_PREFIX + taskId;
    }
}
