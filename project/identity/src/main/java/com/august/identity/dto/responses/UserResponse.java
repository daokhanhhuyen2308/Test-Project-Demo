package com.august.identity.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
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
   @JsonProperty("keycloak_id")
   private String keycloakId;
   private String username;
   private String email;
   private String avatarUrl;
   private Instant createdAt;
   private Instant updatedAt;
   private Set<String> roles;
}
