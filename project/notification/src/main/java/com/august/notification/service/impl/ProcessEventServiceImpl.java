package com.august.notification.service.impl;

import com.august.notification.entity.ProcessedEvent;
import com.august.notification.enums.EventStatus;
import com.august.notification.repository.ProcessedEventRepository;
import com.august.notification.service.ProcessEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessEventServiceImpl implements ProcessEventService {
    private final ProcessedEventRepository processedEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void claim(String eventId, String message) {
        processedEventRepository.claim(eventId, message, Instant.now());
    }

    @Transactional(readOnly = true)
    @Override
    public EventStatus getStatus(String eventId) {
        return processedEventRepository.getStatus(eventId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markProcessed(String eventId) {
        ProcessedEvent event = processedEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("ProcessedEvent not found: " + eventId));
        processedEventRepository.mark(eventId, EventStatus.PROCESSED, null, event.getAttempts(),
                Instant.now(), null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markFailed(String eventId, String error) {
        ProcessedEvent event = processedEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("ProcessedEvent not found: " + eventId));

        int attempts = event.getAttempts() + 1;
        EventStatus status = attempts >= 5 ? EventStatus.DEAD : EventStatus.FAILED;
        Instant nextRetryAt = attempts >= 5 ? null : Instant.now().plusSeconds(300);

        processedEventRepository.mark(eventId, status, error, attempts, Instant.now(), nextRetryAt);
    }

    @Override
    public List<ProcessedEvent> getRetryableEvents() {
        return processedEventRepository.findRetryableEvents(Instant.now(), PageRequest.of(0, 30));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ProcessedEvent> getProcessedEventById(String eventId) {
        return processedEventRepository.findById(eventId);
    }

}
