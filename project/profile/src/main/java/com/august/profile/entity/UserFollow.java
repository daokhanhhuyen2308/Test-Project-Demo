package com.august.profile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_follow",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_follower_following", columnNames = {"follower_id", "following_id"})
        },
        indexes = {
                @Index(name = "idx_user_follow_follower_id", columnList = "follower_id"),
                @Index(name = "idx_user_follow_following_id", columnList = "following_id"),
                @Index(name = "idx_user_follow_following_created_at", columnList = "following_id, created_at desc"),
                @Index(name = "idx_user_follow_follower_created_at", columnList = "follower_id, created_at desc")
        }
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserFollow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false, length = 36)
    private String followerId;

    @Column(name = "following_id", nullable = false, length = 36)
    private String followingId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
