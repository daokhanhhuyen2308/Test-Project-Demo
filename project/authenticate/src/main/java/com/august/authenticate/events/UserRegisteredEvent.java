package com.august.authenticate.events;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRegisteredEvent {
    private String eventId;
    private String keycloakId;
    private String email;
    private String username;
    private String source;
    private LocalDateTime createdAt;
}
