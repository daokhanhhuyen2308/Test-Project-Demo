package com.august.post.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private AuthorResponse author;
    private CategoryResponse category;
    private List<TagResponse> tags;
    private String title;
    private String summary;
    private String content;
    private String slug;
    private String thumbnail;
    private Long viewCount;
    private Integer commentCount;
    private Integer readingTime;
    private String createdAt;
}
