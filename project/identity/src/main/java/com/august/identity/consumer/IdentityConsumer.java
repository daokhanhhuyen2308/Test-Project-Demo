package com.august.identity.consumer;

import com.august.identity.service.UserService;
import com.august.sharecore.events.ProfileCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdentityConsumer {
    private final UserService userService;

    @KafkaListener(topics = "profile-created", groupId = "identity-group")
    public void onProfileCreated(ProfileCreatedEvent profileCreatedEvent){
        userService.consumerProfileCreated(profileCreatedEvent);
    }
}
