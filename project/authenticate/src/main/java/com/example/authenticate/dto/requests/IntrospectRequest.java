package com.example.authenticate.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IntrospectRequest {
    @NotBlank
    private String token;
}
