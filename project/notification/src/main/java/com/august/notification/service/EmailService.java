package com.august.notification.service;

import com.august.notification.dto.EmailDetailRequest;

public interface EmailService {
    void sendEmail(EmailDetailRequest detail);
}
