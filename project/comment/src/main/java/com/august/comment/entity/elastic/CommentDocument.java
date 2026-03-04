package com.august.comment.entity.elastic;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "comments_index")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDocument {
    @Id
    private String id;
    private String postId;
    @Field(type = FieldType.Keyword)
    private String postSlug;
    private String parentCommentId;
    @Field(type = FieldType.Object)
    private AuthorEmbed author;
    private String content;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createdAt;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updatedAt;
    @Builder.Default
    private Integer replyCount = 0;


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorEmbed{
        private String authorId;
        private String authorUsername;
        private String authorAvatarUrl;
    }
}
