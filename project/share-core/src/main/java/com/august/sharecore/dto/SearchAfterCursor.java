package com.august.sharecore.dto;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SearchAfterCursor {
    private Object lastCreatedAt;
    private String lastId;

    public SearchAfterCursor parseObjectToSearchAfterObject(Object[] objects, Class<?> timeType){
        if (objects == null || objects.length < 2) {
            return null;
        }

        Object time;
        Object rawTime = objects[0];

        if (timeType.equals(LocalDateTime.class)) {
            time =  (rawTime instanceof Long l)
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.systemDefault())
                    : (LocalDateTime) rawTime;
        } else {
           time = (rawTime instanceof Long l) ? Instant.ofEpochMilli(l) : (Instant) rawTime;
        }

        return new SearchAfterCursor(time, String.valueOf(objects[1]));
    }
}
