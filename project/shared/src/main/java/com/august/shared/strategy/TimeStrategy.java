package com.august.shared.strategy;

import com.august.shared.enums.TimeUnits;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public interface TimeStrategy {
    TimeUnits getType();
    String formatProcessingTime(LocalDateTime createdAt);
}
