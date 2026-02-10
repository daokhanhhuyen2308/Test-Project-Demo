package com.august.authenticate.dto.requests;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserCreateRequest {
    private String username;
    @Email
    private String email;
    private String password;
    private String avatarUrl;

}
