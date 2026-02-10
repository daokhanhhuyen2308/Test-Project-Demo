package com.august.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class AuthorResponse {
    private String authorId;
    private String authorUsername;
    private String authorAvatarUrl;
}
