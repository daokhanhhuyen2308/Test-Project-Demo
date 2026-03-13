package com.august.post.entity.mssql;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "post", indexes = {
        @Index(name = "idx_post_slug", columnList = "slug", unique = true),
        @Index(name = "idx_post_author_username", columnList = "author_username"),
        @Index(name = "idx_post_created_at", columnList = "created_at")
})
@Builder
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String slug;
    @Column(length = 500, nullable = false)
    private String summary;
    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "author_keycloak_id", nullable = false)
    private String authorKeycloakId;
    @Column(name = "author_username", nullable = false)
    private String authorUsername;
    @Column(name = "author_avatar_url")
    private String authorAvatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEntity> tags;
    private String thumbnail;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "comment_count")
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "reading_time")
    private Integer readingTime;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "favorite_count")
    @Builder.Default
    private Long favoriteCount = 0L;

    @Transient
    @Builder.Default
    @Column(name = "is_favorited")
    private Boolean isFavorited = false;

}
