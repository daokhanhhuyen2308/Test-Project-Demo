package com.august.profile.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "profile")
@Builder
public class UserProfile {
    @Id
    @Column(length = 36, updatable = false, nullable = false)
    private String id;
    @Column(nullable = false, name = "keycloak_id", unique = true)
    private String keycloakId;
    private String username;
    private String email;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @Builder.Default
    @Column(name = "follower_count")
    private Long followerCount = 0L;
    @Builder.Default
    @Column(name = "following_count")
    private Long followingCount = 0L;
    @Column(name = "bio", length = 1000)
    private String bio;
    @Column(name = "created_at",nullable = false)
    private Instant createdAt;
    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.lastModifiedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModifiedAt = Instant.now();
    }
}
