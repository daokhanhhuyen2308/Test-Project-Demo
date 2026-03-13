package com.august.post.entity.elastic;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Document(indexName = "post_index")
public class PostDocument {
    @Id
    @Field(type = FieldType.Keyword)
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
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private Long viewCount;
    private String thumbnail;
    private Long commentCount;
    private Integer readingTime;
    private Integer favoriteCount;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryEmbed {
        private Long id;

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "standard"),
                otherFields = {
                        @InnerField(suffix = "keyword", type = FieldType.Keyword)
                }
        )
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

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "standard"),
                otherFields = {
                        @InnerField(suffix = "keyword", type = FieldType.Keyword)
                }
        )
        private String name;

        @Field(type = FieldType.Keyword)
        private String slug;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorEmbed{
        private String authorKeycloakId;
        @Field(type = FieldType.Keyword)
        private String authorUsername;
        private String authorAvatarUrl;

    }
}
