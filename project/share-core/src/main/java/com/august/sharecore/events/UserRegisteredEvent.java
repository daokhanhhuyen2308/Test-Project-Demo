package com.august.sharecore.events;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent{
     private String eventId;
     private String keycloakId;
     private String email;
     private String username;
     private String avatarUrl;
     private String source;
     private Instant createdAt;
}
