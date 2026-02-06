package com.august.authenticate.controller;

import com.august.authenticate.dto.requests.UserCreateRequest;
import com.august.authenticate.dto.responses.UserResponse;
import com.august.authenticate.service.IdentityService;
import com.august.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class IdentityController {
    private final IdentityService identityService;

    @PostMapping("/create")
    public ApiResponse<UserResponse> createNewUser(@Valid @RequestBody UserCreateRequest userCreateRequest){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(identityService.createUser(userCreateRequest));
        return apiResponse;
    }

}
