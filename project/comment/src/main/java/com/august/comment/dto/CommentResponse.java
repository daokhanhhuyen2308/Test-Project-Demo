package com.august.comment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {
    private String id;
    private String postId;
    private String postSlug;
    private String content;
    private AuthorResponse author;
    private String parentCommentId;
    private List<CommentResponse> replies;
    private Integer replyCount;
    private String createdAt;
    private String updatedAt;
}
