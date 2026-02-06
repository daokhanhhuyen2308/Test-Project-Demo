package com.august.file.dto.requests;

import com.august.file.enums.StatusSendEmail;
import com.august.file.dto.requests.EmailDetailRequest;
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
