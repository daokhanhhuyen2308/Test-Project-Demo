package com.august.shared.strategy.time;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

@Component
public class TimeUnitStrategy {
    private static final NavigableMap<Long, Function<Long, String>> strategies = new TreeMap<>();

    static {
        strategies.put(0L, seconds -> {
            if (seconds <= 5) return "Just now";
            return seconds + "s ago";
        });
        strategies.put(60L, seconds -> (seconds / 60) + "m ago");
        strategies.put(3600L, seconds -> (seconds / 3600) + "h ago");
        strategies.put(86400L, seconds -> (seconds / 86400) + "d ago");
        strategies.put(604800L, seconds -> (seconds / 604800) + "w ago");
        strategies.put(2592000L, seconds -> (seconds / 2592000) + "mo ago");
        strategies.put(31536000L, seconds -> (seconds / 31536000) + "y ago");
    }
    public String processTimeUnitStrategy(LocalDateTime createdAt){
        Long seconds = ChronoUnit.SECONDS.between(createdAt, LocalDateTime.now());
        Map.Entry<Long, Function<Long, String>> floorEntry = strategies.floorEntry(seconds);
        Function<Long, String> functionValue = floorEntry.getValue();
        return functionValue.apply(seconds);
    }
}
