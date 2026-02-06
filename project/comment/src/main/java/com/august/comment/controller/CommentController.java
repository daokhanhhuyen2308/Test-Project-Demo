package com.august.comment.controller;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.service.CommentService;
import com.august.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentResponse> createCommentPost(@RequestBody @Valid CommentRequest request,
                                                          @AuthenticationPrincipal Jwt jwt){
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createCommentPost(request, jwt))
                .build();
    }
}
