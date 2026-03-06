package com.august.identity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventTopic {
    USER_REGISTERED("user-registered-v2"),
    PROFILE_CREATED("profile-created");

    private final String kafkaTopicName;

}
