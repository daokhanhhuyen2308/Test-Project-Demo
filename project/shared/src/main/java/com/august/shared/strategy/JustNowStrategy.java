package com.august.shared.strategy;

import com.august.shared.enums.TimeUnits;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Order(1)
public class JustNowStrategy implements TimeStrategy{
    @Override
    public TimeUnits getType() {
        return TimeUnits.JUST_NOW;
    }

    @Override
    public String formatProcessingTime(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(createdAt, now);
        if (seconds <= 0){
            return "Just now";
        }
        return seconds + "s ago";
    }
}
