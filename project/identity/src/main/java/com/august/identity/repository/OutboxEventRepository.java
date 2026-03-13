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
            "o.processedAt = :processedAt, " +
            "o.lastError = :lastError, " +
            "o.retryCount = :newRetry " +
            "where o.id = :id")
    void updateStatusWithProcessedAt(@Param("id") String id, @Param("status") OutboxStatus status,
                                     @Param("processedAt") Instant processedAt,
                                     @Param("lastError") String lastError,
                                     @Param("newRetry") Integer newRetry);

    @Query("select o from OutboxEvent o where o.outboxStatus in :listStatus " +
            "and o.createdAt <= :timeLimit and o.retryCount <= 5 order by o.createdAt asc")
    List<OutboxEvent> findEventsToRetry(@Param("timeLimit") Instant timeLimit,
                                        @Param("listStatus") List<OutboxStatus> listStatus,
                                        Pageable pageable);
}
