package com.august.identity.entity;

import com.august.identity.enums.AggregateType;
import com.august.identity.enums.EventTopic;
import com.august.identity.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "outbox_event", indexes = {
        @Index(name = "idx_outbox_status_created", columnList = "outbox_status, created_at"),
        @Index(name = "idx_outbox_aggregate", columnList = "aggregate_type, aggregate_id")
})
public class OutboxEvent {
    @Id
    @Column(columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private String id;
    @Enumerated(EnumType.STRING)
    private AggregateType aggregateType;
    private String aggregateId;
    @Enumerated(EnumType.STRING)
    private EventTopic topic;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus outboxStatus;
    private Instant createdAt;
    private Instant processedAt;
    private Instant sentAt;
    private Integer retryCount;
    private String lastError;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (outboxStatus == null) outboxStatus = OutboxStatus.PENDING;
        if (retryCount == null) retryCount = 0;
        if (processedAt == null) processedAt = Instant.now();
    }
}
