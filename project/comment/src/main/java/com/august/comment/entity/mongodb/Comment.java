package com.august.comment.entity.mongodb;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection = "comments")
@CompoundIndex(name = "index_post_slug_created_at", def = "{'postSlug: 1', 'createdAt: -1'}")
public class Comment {
    @Id
    private String id;
    private String content;
    @Field(name = "author_id")
    private String authorId;
    @Field(name = "author_username")
    private String authorUsername;
    @Field(name = "author_avatar_url")
    private String authorAvatarUrl;
    private Long postId;
    private String postSlug;
    @Indexed
    private String parentCommentId;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    @Field("reply_count")
    private Integer replyCount;
}
