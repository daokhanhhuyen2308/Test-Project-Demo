package com.august.file.service;

import com.august.file.dto.requests.EmailDetailRequest;

public interface EmailService {
    void sendEmail(EmailDetailRequest detail);
}
