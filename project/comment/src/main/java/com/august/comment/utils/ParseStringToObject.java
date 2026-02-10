package com.august.comment.utils;


import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParseStringToObject {
    public static Object[] parse(String cursor){
        if (cursor == null){
            return null;
        }

        Object[] parts = cursor.split(",");
        if (parts.length != 2){
            throw new AppCustomException(ErrorCode.INVALID_INPUT);
        }

        String timeString = parts[0].toString().trim();
        String idString = parts[1].toString().trim();

        LocalDateTime lastCreatedAt;
        try{
            lastCreatedAt = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME);
        }catch (Exception e){
            throw new AppCustomException(ErrorCode.UPLOAD_FILE_ERROR);
        }
       return new Object[]{lastCreatedAt, idString};

    }
}
