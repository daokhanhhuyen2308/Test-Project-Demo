package com.example.demo.service;

import com.example.demo.dto.EmailDetailRequest;

public interface EmailService {
    void sendEmail(EmailDetailRequest detail);
}
