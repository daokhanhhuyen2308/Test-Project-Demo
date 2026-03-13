package com.august.notification.scheduler;

import com.august.notification.entity.ProcessedEvent;
import com.august.notification.service.NotificationService;
import com.august.notification.service.ProcessEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationJobScheduler {
    private final ProcessEventService processEventService;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 300000)
    public void retryFailedNotifications() {
        List<ProcessedEvent> events = processEventService.getRetryableEvents();

        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} retryable events", events.size());
        events.forEach(event -> {
            try {
                notificationService.retryUserRegistration(event.getEventId());
            } catch (Exception ex) {
                log.error("Retry failed for eventId={}", event.getEventId(), ex);
            }
        });
    }

}
