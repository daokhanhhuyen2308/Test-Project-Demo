package com.august.shared.strategy;

import com.august.shared.enums.TimeUnits;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Order(6)
public class WeekStrategy implements TimeStrategy {
    @Override
    public TimeUnits getType() {
        return TimeUnits.WEEK;
    }

    @Override
    public String formatProcessingTime(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.WEEKS.between(createdAt, now);
        return minutes + "ago";
    }
}
