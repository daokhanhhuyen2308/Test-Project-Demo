package com.august.notification.service;

import com.august.notification.events.UserRegisteredEvent;

public interface NotificationService {
    void processUserRegistration(UserRegisteredEvent event);
}
