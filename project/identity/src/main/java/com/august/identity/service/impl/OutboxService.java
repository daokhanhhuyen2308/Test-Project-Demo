package com.august.identity.service.impl;

import com.august.identity.enums.OutboxStatus;
import com.august.identity.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateOutboxStatus(String outboxId, OutboxStatus status, String lastError, Integer newRetry) {
        outboxEventRepository.updateStatusWithProcessedAt(outboxId, status, Instant.now(), lastError, newRetry);
    }
}
