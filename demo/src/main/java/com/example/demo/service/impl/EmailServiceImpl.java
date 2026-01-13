package com.example.demo.service.impl;

import com.example.demo.dto.EmailDetail;
import com.example.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    @Override
    public void sendEmail(EmailDetail detail) {

        try {
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();

            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(detail.getRecipient());
            mailMessage.setText(detail.getMsgBody());
            mailMessage.setSubject(detail.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            logger.info("Send email successfully");
        }

        catch (Exception e) {
            throw new RuntimeException("Went wrong when sending email!");
        }


    }
}
