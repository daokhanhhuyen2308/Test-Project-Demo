package com.august.file.cronjob;

import com.august.file.enums.StatusSendEmail;
import com.august.file.service.EmailService;
import com.august.file.service.impl.EmailTaskRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.logging.Logger;

@Component
@RequiredArgsConstructor
public class EmailTaskCronJob {

    private final EmailTaskRedisService emailTaskRedisService;
    private final EmailService emailService;

    private static final Logger logger = Logger.getLogger((EmailTaskCronJob.class.getName()));

    @Scheduled(fixedDelay = 60000)
    public void runCronJob(){

         emailTaskRedisService.findAll()
                 .stream()
                 .filter(task -> task.getStatus() == StatusSendEmail.EMAIL_PENDING ||
                                        task.getStatus() == StatusSendEmail.EMAIL_RETRY)
                 .filter(task -> task.getNextRetryAt() == null || !task.getNextRetryAt().isAfter(Instant.now()))
                 .map(task -> {
                     try {
                         emailService.sendEmail(task.getRequest());
                         task.setStatus(StatusSendEmail.EMAIL_SENT);
                         logger.info("Email sent successfully and removed from Redis: {}" + task.getId());
                         emailTaskRedisService.delete(task.getId());
                     } catch (Exception e) {
                         int retryCount = task.getRetryCount() + 1;
                         task.setRetryCount(retryCount);

                         if (retryCount >= task.getMaxRetryCount()) {
                             task.setStatus(StatusSendEmail.EMAIL_FAILED);
                         } else {
                             task.setStatus(StatusSendEmail.EMAIL_RETRY);
                             task.setNextRetryAt(Instant.now().plusSeconds(300));
                         }
                         emailTaskRedisService.update(task);
                     }
                     return null;
                 }).toList();
    }
}
