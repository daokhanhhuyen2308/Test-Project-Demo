package com.august.identity.repository;

import com.august.identity.entity.OutboxEvent;
import com.august.identity.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    @Modifying
    @Query("update OutboxEvent o set o.outboxStatus = :status, " +
            "o.processedAt = :processed_at, " +
            "o.lastError = :lastError " +
            "where o.id = :id")
    void updateStatusWithProcessedAt(@Param("id") String id, @Param("status") OutboxStatus status,
                                     @Param("processed_at") Instant processedAt,
                                     @Param("lastError") String lastError);

    @Query("select o from OutboxEvent o where o.outboxStatus = :status " +
            "and o.createdAt <= :timeLimit order by o.createdAt asc")
    List<OutboxEvent> findAllPendingOlderThan(@Param("status") OutboxStatus status,
                                              @Param("timeLimit") Instant timeLimit,
                                              Pageable pageable);
}
