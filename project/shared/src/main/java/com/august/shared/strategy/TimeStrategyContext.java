package com.august.shared.strategy;

import com.august.shared.enums.TimeUnits;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TimeStrategyContext {
    private final Map<TimeUnits, TimeStrategy> strategies;

    public TimeStrategyContext(List<TimeStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(TimeStrategy::getType, Function.identity()));
    }

    public String executeStrategy(LocalDateTime createdAt){
        if (createdAt == null) return "Just now";

        LocalDateTime now = LocalDateTime.now();
        long totalSeconds = ChronoUnit.SECONDS.between(createdAt, now);

        TimeUnits timeUnits = getTimeUnitsFromSeconds(totalSeconds);

        TimeStrategy timeStrategy = strategies.get(timeUnits);

        return timeStrategy.formatProcessingTime(createdAt);
    }

    private TimeUnits getTimeUnitsFromSeconds(long totalSeconds){
        if (totalSeconds <= 0) return TimeUnits.JUST_NOW;
        if (totalSeconds < 60) return TimeUnits.SECOND;
        if (totalSeconds < 3600) return TimeUnits.MINUTE;
        if (totalSeconds < 86400) return TimeUnits.HOUR;
        if (totalSeconds < 604800) return TimeUnits.DAY;
        if (totalSeconds < 2419200) return TimeUnits.WEEK;
        if (totalSeconds < 31536000) return TimeUnits.MONTH;
        return TimeUnits.YEAR;
    }
}
