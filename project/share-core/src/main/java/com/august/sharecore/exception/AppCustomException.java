package com.august.sharecore.exception;

import com.august.sharecore.enums.ErrorCode;
import lombok.Getter;

@Getter
public class AppCustomException extends RuntimeException{
    private final ErrorCode errorCode;

    public AppCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
