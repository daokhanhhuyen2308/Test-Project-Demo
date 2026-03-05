package com.august.notification.service.impl;

import com.august.notification.dto.EmailDetailRequest;
import com.august.notification.enums.EventStatus;
import com.august.sharecore.events.UserRegisteredEvent;
import com.august.notification.repository.ProcessedEventRepository;
import com.august.notification.service.EmailService;
import com.august.notification.service.NotificationService;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final ProcessedEventRepository processedEventRepository;

    @Override
    public void processUserRegistration(UserRegisteredEvent event) {

        String eventId = event.eventId();
        processedEventRepository.claim(eventId);

        if (processedEventRepository.getStatus(eventId) == EventStatus.PROCESSED){
            log.warn("eventId={} already PROCESSED -> skip", eventId);
            return;
        }

        try{
            EmailDetailRequest request = EmailDetailRequest.builder()
                    .subject("Welcome to " +event.username())
                    .msgBody("Hi " + event.username() + ",\n\n" +
                            "Your account has been successfully created at " + event.createdAt())
                    .recipient(event.email())
                    .build();

            emailService.sendEmail(request);

            processedEventRepository.mark(eventId, EventStatus.PROCESSED, null, Instant.now());
        } catch (Exception ex) {
            processedEventRepository.mark(eventId, EventStatus.FAILED,
                    ex.getClass().getSimpleName() + ": " + safe(ex.getMessage()),
                    Instant.now());
            throw new AppCustomException(ErrorCode.EMAIL_DELIVERY_FAILED);
        }

    }

    private String safe(String msg){
        if (msg == null) return "";
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
