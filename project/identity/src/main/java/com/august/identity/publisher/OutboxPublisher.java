package com.august.identity.publisher;

import com.august.identity.entity.OutboxEvent;
import com.august.identity.enums.OutboxStatus;
import com.august.identity.repository.OutboxEventRepository;
import com.august.identity.service.impl.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "user-registered-v2";
    private final OutboxService outboxService;

    @Scheduled(fixedDelay = 3000)
    public void publishEventPending(){

        Instant timeLimit = Instant.now().minusSeconds(20);
        List<OutboxEvent> events = outboxEventRepository.findEventsToRetry(timeLimit,
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
                PageRequest.of(0, 30));

        if (events.isEmpty()) return;

        events.forEach(event -> event.setOutboxStatus(OutboxStatus.PROCESSING));
        outboxEventRepository.saveAll(events);

        events.forEach(event -> {
                    try{
                        String payload = event.getPayload();

                        kafkaTemplate.send(TOPIC, event.getAggregateId(), payload)
                                .whenComplete((result, ex) -> {
                                    if (ex == null){
                                        outboxService.updateOutboxStatus(event.getId(), OutboxStatus.SENT,
                                                null, event.getRetryCount());
                                        log.info("Published {} to topic {}", event.getAggregateType(), TOPIC);
                                    }
                                    else {
                                        int newRetry = event.getRetryCount() + 1;
                                        outboxService.updateOutboxStatus(event.getId(), OutboxStatus.FAILED,
                                                ex.getMessage(), newRetry);
                                        event.setLastError(ex.getMessage());
                                        log.error("Publish failed for {}: {}", event.getAggregateType(),
                                                ex.getMessage());
                                    }
                                });


                        log.info("Publisher event {} to topic: {}", event.getAggregateType(), TOPIC);
                    } catch (Exception e) {
                        outboxService.updateOutboxStatus(event.getId(), OutboxStatus.FAILED, e.getMessage(),
                                event.getRetryCount() + 1);
                        log.error("Outbox parse/publish failed: {}", e.getMessage(), e);
                    }
                });
    }

}
