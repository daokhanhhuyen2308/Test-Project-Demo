package com.august.post.dto;

import lombok.*;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private Long id;
    private String tag;
    private String slug;
}
