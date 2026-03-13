package com.august.notification.service;

import com.august.notification.entity.ProcessedEvent;
import com.august.notification.enums.EventStatus;

import java.util.List;
import java.util.Optional;


public interface ProcessEventService {
    void claim(String eventId, String message);

    EventStatus getStatus(String eventId);

    void markProcessed(String eventId);

    void markFailed(String eventId, String s);

    List<ProcessedEvent> getRetryableEvents();

    Optional<ProcessedEvent> getProcessedEventById(String eventId);

}
