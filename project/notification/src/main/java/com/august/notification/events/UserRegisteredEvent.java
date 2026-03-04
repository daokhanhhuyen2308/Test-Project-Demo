package com.august.notification.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisteredEvent {
    private String eventId;
    private String keycloakId;
    private String email;
    private String username;
    private LocalDateTime createdAt;
    private String source;
}
