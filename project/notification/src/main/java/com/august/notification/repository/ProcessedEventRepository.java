package com.august.notification.repository;

import com.august.notification.entity.ProcessedEvent;
import com.august.notification.enums.EventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {


    @Modifying
    @Query(value = """
            insert into processed_event (event_id, status, attempts, last_error, updated_at, next_retry_at, payload)
            values (:eventId, 'PROCESSING', 0, NULL, :now, NULL, :payload)
            on duplicate key update updated_at = processed_event.updated_at, payload = COALESCE(payload, :payload)
    """, nativeQuery = true)
    void claim(@Param("eventId") String eventId, @Param("payload") String message, @Param("now") Instant now);

    @Query("select p.status from ProcessedEvent p where p.eventId = :eventId")
    EventStatus getStatus(@Param("eventId") String eventId);

    @Modifying
    @Query("""
        update ProcessedEvent p
        set p.status = :status,
            p.lastError = :error,
            p.attempts = :attempts,
            p.updatedAt = :now,
            p.nextRetryAt = :nextRetryAt
        where p.eventId = :eventId
    """)
    void mark(@Param("eventId") String eventId,
              @Param("status") EventStatus status,
              @Param("error") String error,
              @Param("attempts") int attempts,
              @Param("now") Instant now,
              @Param("nextRetryAt") Instant nextRetryAt);


    @Query("""
        select p
        from ProcessedEvent p
        where p.status = 'FAILED'
          and p.attempts < 5
          and p.nextRetryAt is not null
          and p.nextRetryAt <= :now
        order by p.updatedAt asc
    """)
    List<ProcessedEvent> findRetryableEvents(@Param("now") Instant now, Pageable pageable);
}
