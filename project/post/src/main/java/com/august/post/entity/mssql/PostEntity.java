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
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_slug", columnList = "slug", unique = true),
        @Index(name = "idx_post_author", columnList = "author_id")
})
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String slug;
    @Column(length = 500)
    private String summary;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "author_id", nullable = false)
    private String authorId;
    @Column(name = "author_username", nullable = false)
    private String authorUsername;

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

    @Column(name = "view_count", columnDefinition = "bigint default 0")
    private Long viewCount = 0L;

    @Column(name = "comment_count", columnDefinition = "int default 0")
    private Integer commentCount = 0;

    @Column(name = "reading_time")
    private Integer readingTime;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_paid")
    private Boolean isPaid = false;
    private Boolean isDeleted = false;

}
