package com.august.authenticate.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
   private String id;
   private String username;
   private String email;
   private String avatarUrl;
   private Instant createdAt;
   private Instant updatedAt;
   private Set<String> roles;
}
