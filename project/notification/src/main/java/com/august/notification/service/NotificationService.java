package com.august.notification.service;


public interface NotificationService {
    void processUserRegistration(String message);
    void retryUserRegistration(String eventId);
}
