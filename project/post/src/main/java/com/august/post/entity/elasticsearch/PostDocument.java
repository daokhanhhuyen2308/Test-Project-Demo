package com.august.post.entity.elasticsearch;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Document(indexName = "post_index")
public class PostDocument {
    @Id
    private String id;
    @Field(type = FieldType.Object)
    private AuthorEmbed author;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String slug;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String summary;
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String content;
    @Field(type = FieldType.Object)
    private CategoryEmbed category;
    @Field(type = FieldType.Object)
    private List<TagEmbed> tags;
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private LocalDateTime createdAt;
    private Boolean isDeleted;
    private Long viewCount;
    private String thumbnail;
    private Integer commentCount;
    private Integer readingTime;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryEmbed {
        private Long id;

        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Keyword)
        private String slug;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagEmbed {
        private Long id;

        @Field(type = FieldType.Keyword)
        private String name;

        @Field(type = FieldType.Keyword)
        private String slug;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorEmbed{
        private String authorId;
        @Field(type = FieldType.Keyword)
        private String authorUsername;
        private String authorAvatarUrl;

    }
}
