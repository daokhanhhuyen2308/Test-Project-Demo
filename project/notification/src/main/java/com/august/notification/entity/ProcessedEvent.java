package com.august.notification.entity;

import com.august.notification.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "processed_event")
public class ProcessedEvent {
    @Id
    @Column(name = "event_id")
    private String eventId;
    @Enumerated(value = EnumType.STRING)
    private EventStatus status;
    private int attempts;
    @Column(name = "last_error")
    private String lastError;
    @Column(name = "updated_at")
    private Instant updatedAt;
    @Column(name = "next_retry_at")
    private Instant nextRetryAt;
    @Column(columnDefinition = "LONGTEXT")
    private String payload;
}
