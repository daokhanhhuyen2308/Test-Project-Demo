package com.august.notification.repository;

import com.august.notification.entity.ProcessedEvent;
import com.august.notification.enums.EventStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {


    @Modifying
    @Query(value = """
    insert into process_event (event_id, updated_at, attempts, last_error, status)
    values (:eventId, now(), 1, null, 'PROCESSING')
    on duplicate key update status = if(status = 'PROCESSED', status, 'PROCESSING'),
                            attempts = if(status = 'PROCESSED', attempts, attempts + 1),
                            updated_at = now()
    """, nativeQuery = true)
    void claim(@Param("eventId") String eventId);

    @Query("select p.status from ProcessedEvent p where p.eventId = :eventId")
    EventStatus getStatus(@Param("eventId") String eventId);

    @Modifying
    @Query("""
     update ProcessedEvent p set p.status = :status, p.lastError = :error, p.updatedAt = :now
      where p.eventId = :eventId
    """)
    void mark(@Param("eventId") String eventId,
              @Param("status") EventStatus status,
              @Param("error") String error,
              @Param("now")Instant now);

}
