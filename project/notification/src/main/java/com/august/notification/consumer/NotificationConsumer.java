package com.august.notification.consumer;

import com.august.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationService notificationService;
    @KafkaListener(topics = "user-registered-v2", groupId = "notification-group")
    public void consumerUserRegistered(String message, Acknowledgment ack){
        try {
            log.info("Raw message: {}", message);
            notificationService.processUserRegistration(message);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to consume user-registered message", e);
            throw new RuntimeException(e);
        }
    }
}
