package com.august.identity.listener;

import com.august.identity.enums.OutboxStatus;
import com.august.identity.events.OutboxNotificationEvent;
import com.august.identity.repository.OutboxEventRepository;
import com.august.identity.service.impl.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventListener {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxService outboxService;

    //    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @EventListener
    public void handleOutboxNotificationEvent(OutboxNotificationEvent event) {
        log.info("==> Received internal notification event for ID: {}", event.outboxId());
        outboxEventRepository.findById(event.outboxId()).ifPresent(outbox -> {
            log.info("==> Attempting to send Kafka message to topic: {}", outbox.getTopic().getKafkaTopicName());
            kafkaTemplate.send(outbox.getTopic().getKafkaTopicName(), outbox.getAggregateId(), outbox.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            outboxService.updateOutboxStatus(outbox.getId(), OutboxStatus.SENT, null);
                            log.info("Event ID: {} successfully dispatched to Kafka", outbox.getId());
                        } else {
                            log.error("Failed to dispatch Event ID: {}. Reason: {}. Scheduled task will retry.",
                                    outbox.getId(), ex.getMessage());
                        }
                    });
        });
    }
}
