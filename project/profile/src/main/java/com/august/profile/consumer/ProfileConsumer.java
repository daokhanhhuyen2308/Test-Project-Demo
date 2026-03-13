package com.august.profile.consumer;

import com.august.profile.service.UserProfileService;
import com.august.sharecore.events.UserRegisteredEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileConsumer {

    private final UserProfileService userProfileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-registered-v2", groupId = "profile-group")
    public void onUserRegistered(String message, Acknowledgment ack){
        try {
            log.info("Raw message: {}", message);

            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);

            userProfileService.createProfileForUserRegistered(event);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to consume user-registered message", e);
            throw new RuntimeException(e);
        }
    }
}
