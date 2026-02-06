package com.august.comment.service.impl;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.entity.Comment;
import com.august.comment.mapper.CommentMapper;
import com.august.comment.repository.CommentRepository;
import com.august.comment.service.CommentService;
import com.august.shared.strategy.TimeStrategyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final TimeStrategyContext timeStrategyContext;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${KEY.COMMENT.COUNT}")
    private String KEY_COMMENT_COUNT;

    @Override
    public CommentResponse createCommentPost(CommentRequest request, Jwt jwt) {
        String authorId = jwt.getSubject();
        String authorUsername = jwt.getClaimAsString("preferred_username");

        Comment comment = commentMapper.mapToComment(request);
        comment.setAuthorId(authorId);
        comment.setAuthorUsername(authorUsername);
        Comment savedComment = commentRepository.save(comment);

        String keyCmtCount = KEY_COMMENT_COUNT + request.getPostId();
        redisTemplate.opsForValue().increment(keyCmtCount);

        CommentResponse response = commentMapper.mapToCmtResponse(savedComment);
        response.setCreatedAt(timeStrategyContext.executeStrategy(comment.getCreatedAt()));

        return response;
    }
}
