package com.august.sharecore.events;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreatedEvent{
    private String eventId;
    private String keycloakId;
    private String source;
    private Instant createdAt;
}
