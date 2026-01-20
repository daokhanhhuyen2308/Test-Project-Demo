package com.example.demo.service;

import com.example.demo.dto.requests.EmailDetailRequest;
import com.example.demo.dto.requests.EmailTaskDTO;
import com.example.demo.enums.StatusSendEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class EmailTaskCronJob {

    private final EmailTaskRedisService emailTaskRedisService;
    private final EmailService emailService;

    private static final Logger logger = Logger.getLogger((EmailTaskCronJob.class.getName()));

    @Scheduled(fixedDelay = 60000)
    public void runCronJob(){
        List<EmailTaskDTO> emailTasks = emailTaskRedisService.findAll();

        emailTasks.stream()
                .filter(task -> task.getStatus() == StatusSendEmail.EMAIL_PENDING ||
                                task.getStatus() == StatusSendEmail.EMAIL_RETRY)
                .filter(task -> task.getNextRetryAt() == null || !task.getNextRetryAt().isAfter(Instant.now()))
                .forEach(task -> {
                    try{
                        EmailDetailRequest request = task.getRequest();

                        emailService.sendEmail(request);
                        task.setStatus(StatusSendEmail.EMAIL_SENT);
                        emailTaskRedisService.delete(task.getId());
                        logger.info("Email sent successfully and removed from Redis: {}" + task.getId());

                    } catch (Exception e) {
                        int retryCount = task.getRetryCount() + 1;
                        task.setRetryCount(retryCount);

                        if (retryCount >= task.getMaxRetryCount()){
                            task.setStatus(StatusSendEmail.EMAIL_FAILED);
                        }
                        else {
                            task.setStatus(StatusSendEmail.EMAIL_RETRY);
                            task.setNextRetryAt(Instant.now().plusSeconds(300));
                        }

                        emailTaskRedisService.update(task);
                    }
                });
    }
}
