package com.august.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreationRequest {
    @NotBlank(message = "Title is not empty")
    private String title;
    private String summary;
    @NotBlank(message = "Nội dung đâu ông ơi?")
    private String content;
    private Long categoryId;
    private List<String> tags;
    private String thumbnail;
}
