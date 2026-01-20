package com.example.demo.dto.requests;

import com.example.demo.enums.StatusSendEmail;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class EmailTaskDTO {
    private String id;
    private EmailDetailRequest request;
    private StatusSendEmail status;
    private int retryCount;
    private int maxRetryCount;
    private Instant nextRetryAt;

}
