package com.august.notification.controller;

import com.august.notification.events.UserRegisteredEvent;
import com.august.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationService notificationService;

    @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 2000, multiplier = 2.0, maxDelay = 60000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "user-registration", groupId = "notification-groups")
    public void consumerUserRegistered(UserRegisteredEvent event){
        log.info("Consumer received event: {}", event.getEventId());
        notificationService.processUserRegistration(event);
    }
}
