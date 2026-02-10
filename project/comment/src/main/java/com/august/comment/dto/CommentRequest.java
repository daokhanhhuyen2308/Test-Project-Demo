package com.august.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CommentRequest {
    @NotNull(message = "Post ID cannot be null")
    private Long postId;
    @NotBlank(message = "Content cannot be empty")
    @Size(max = 500, message = "Comment too long")
    private String content;
    private String parentCommentId;
}
