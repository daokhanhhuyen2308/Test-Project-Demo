package com.august.profile.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "profile")
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;
    @Column(nullable = false)
    private String keycloakId;
    private String username;
    private String email;
    private String avatarUrl;
}
