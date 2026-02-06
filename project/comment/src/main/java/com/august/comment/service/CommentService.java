package com.august.comment.service;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface CommentService {
    CommentResponse createCommentPost(CommentRequest request, Jwt jwt);
}
