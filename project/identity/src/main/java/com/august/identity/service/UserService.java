package com.august.identity.service;

import com.august.identity.dto.requests.UserCreateRequest;
import com.august.identity.dto.responses.UserResponse;
import com.august.sharecore.events.ProfileCreatedEvent;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);

    void consumerProfileCreated(ProfileCreatedEvent profileCreatedEvent);
}
