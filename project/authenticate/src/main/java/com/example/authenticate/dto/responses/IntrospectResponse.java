package com.example.authenticate.dto.responses;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IntrospectResponse {
    private boolean isValid;
}
