package com.august.identity.dto.requests;

import com.august.identity.validator.EmailConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @EmailConstraint
    private String email;
    @NotBlank(message = "Password can not be empty")
    private String password;
}
