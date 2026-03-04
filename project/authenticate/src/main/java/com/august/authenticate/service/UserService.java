package com.august.authenticate.service;

import com.august.authenticate.dto.requests.UserCreateRequest;
import com.august.authenticate.dto.responses.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreateRequest userCreateRequest);
}
