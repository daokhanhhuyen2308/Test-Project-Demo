package com.august.shared.exception;

import com.august.shared.dto.ApiResponse;
import com.august.shared.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppCustomException.class)
    public ResponseEntity<ApiResponse<?>> handleAppCustomException(AppCustomException exception){
        return buildResponse(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotValidException(){
        return buildResponse(ErrorCode.METHOD_REQUEST_INVALID);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(){
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode) {
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        apiResponse.setTimestamp(Instant.now());
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }

}
