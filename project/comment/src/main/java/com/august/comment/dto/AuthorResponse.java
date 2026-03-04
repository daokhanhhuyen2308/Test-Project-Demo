package com.august.comment.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AuthorResponse {
    private String authorId;
    private String authorUsername;
    private String authorAvatarUrl;
}
