package com.august.shared.exception;

import com.august.shared.enums.ErrorCode;
import lombok.Getter;

@Getter
public class AppCustomException extends RuntimeException{
    private final ErrorCode errorCode;

    public AppCustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
