package com.august.authenticate.controller;

import com.august.authenticate.dto.requests.UserCreateRequest;
import com.august.authenticate.dto.responses.UserResponse;
import com.august.authenticate.service.UserService;
import com.august.sharecore.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping("/create-user")
    public ApiResponse<UserResponse> createNewUser(@Valid @RequestBody UserCreateRequest userCreateRequest){
        ApiResponse<UserResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(userService.createUser(userCreateRequest));
        return apiResponse;
    }

    @PostMapping("/create")
    public String test() {
        System.out.println("ĐÂY RỒI!");
        return "OK";
    }

}
