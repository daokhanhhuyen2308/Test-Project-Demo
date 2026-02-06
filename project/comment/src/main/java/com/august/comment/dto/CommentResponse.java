package com.august.comment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private String commentId;
    private String content;
    private String parentCmtId;
    private String createdAt;


}
