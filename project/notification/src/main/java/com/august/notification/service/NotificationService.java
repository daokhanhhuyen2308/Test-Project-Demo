package com.august.notification.service;

import com.august.sharecore.events.UserRegisteredEvent;

public interface NotificationService {
    void processUserRegistration(UserRegisteredEvent event);
}
