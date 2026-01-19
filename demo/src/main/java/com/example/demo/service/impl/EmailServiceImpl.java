package com.example.demo.service.impl;

import com.example.demo.dto.EmailDetailRequest;
import com.example.demo.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    @Override
    public void sendEmail(EmailDetailRequest detail) {

        try {
            MimeMessage mailMessage
                    = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "UTF-8");

            String htmlMsg = "<h3>Welcome!</h3>"
                    + "<p>You have uploaded the file successfully.</p>";

            helper.setFrom(sender);
            helper.setTo(detail.getRecipient());
            helper.setText(htmlMsg, true);
            helper.setSubject(detail.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            logger.info("Send email successfully");
        }

        catch (Exception e) {
            throw new RuntimeException("Went wrong when sending email!");
        }


    }
}
