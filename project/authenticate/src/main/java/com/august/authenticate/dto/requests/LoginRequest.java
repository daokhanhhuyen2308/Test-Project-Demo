package com.august.authenticate.dto.requests;

import com.august.authenticate.validator.EmailConstraint;
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
