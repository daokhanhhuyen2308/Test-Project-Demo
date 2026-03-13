package com.august.notification.service.impl;

import com.august.notification.dto.EmailDetailRequest;
import com.august.notification.entity.ProcessedEvent;
import com.august.notification.enums.EventStatus;
import com.august.notification.service.ProcessEventService;
import com.august.sharecore.events.UserRegisteredEvent;
import com.august.notification.service.EmailService;
import com.august.notification.service.NotificationService;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final EmailService emailService;
    private final ProcessEventService processEventService;
    private final ObjectMapper objectMapper;

    @Override
    public void processUserRegistration(String message) {

        UserRegisteredEvent event = parseEvent(message);
        String eventId = event.getEventId();
        processEventService.claim(eventId, message);

        if (processEventService.getStatus(eventId) == EventStatus.PROCESSED){
            log.warn("eventId={} already PROCESSED -> skip", eventId);
            return;
        }

        sendWelcomeEmail(event);
    }

    @Override
    public void retryUserRegistration(String eventId) {
        ProcessedEvent processedEvent = processEventService.getProcessedEventById(eventId)
                .orElseThrow(() -> new AppCustomException(ErrorCode.PROCESSED_EVENT_NOT_FOUND, eventId));

        UserRegisteredEvent event = parseEvent(processedEvent.getPayload());
        sendWelcomeEmail(event);
    }

    private String safe(String msg){
        if (msg == null) return "";
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }

    private UserRegisteredEvent parseEvent(String message) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);
            log.info("Consumer received event: {}", event.getEventId());
            return event;
        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.CANNOT_SERIALIZE_EVENT);
        }
    }

    private void sendWelcomeEmail(UserRegisteredEvent event) {
        try {
            EmailDetailRequest request = EmailDetailRequest.builder()
                    .subject("Welcome to " + event.getUsername())
                    .msgBody("Hi " + event.getUsername() + ",\n\n" +
                            "Your account has been successfully created at " + event.getCreatedAt())
                    .recipient(event.getEmail())
                    .build();

            emailService.sendEmail(request);
            processEventService.markProcessed(event.getEventId());

            log.info("Processed successfully eventId={}", event.getEventId());
        } catch (Exception ex) {
            processEventService.markFailed(
                    event.getEventId(),
                    ex.getClass().getSimpleName() + ": " + safe(ex.getMessage())
            );
            throw new AppCustomException(ErrorCode.EMAIL_DELIVERY_FAILED);
        }
    }
}
