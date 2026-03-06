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
@Table(name = "process_event")
public class ProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String eventId;
    @Enumerated(value = EnumType.STRING)
    private EventStatus status;
    private int attempts;
    private String lastError;
    private Instant updatedAt;

}
