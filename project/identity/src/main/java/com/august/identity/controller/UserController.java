package com.august.identity.controller;

import com.august.identity.dto.requests.UserCreateRequest;
import com.august.identity.dto.responses.UserResponse;
import com.august.identity.service.UserService;
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
        return ApiResponse.success(userService.createUser(userCreateRequest),
                "Create account successfully");
    }

    @PostMapping("/create")
    public String test() {
        System.out.println("ĐÂY RỒI!");
        return "OK";
    }

}
