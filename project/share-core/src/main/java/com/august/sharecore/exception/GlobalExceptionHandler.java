package com.august.sharecore.exception;

import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppCustomException.class)
    public ResponseEntity<ApiResponse<?>> handleAppCustomException(AppCustomException exception, Exception e){
        return buildResponse(exception.getErrorCode(), e);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodNotValidException(){
        return buildResponse(ErrorCode.METHOD_REQUEST_INVALID, null);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e){
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, e);
    }

    public static ResponseEntity<ApiResponse<?>> buildResponse(ErrorCode errorCode, Exception ex) {
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        apiResponse.setTimestamp(Instant.now());
        apiResponse.setStatus(errorCode.getStatusCode());

        if (ex != null) {
            apiResponse.setTrace(getStackTraceStatic(ex));
        }

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }

    private static String getStackTraceStatic(Exception ex) {
        StringBuilder sb = new StringBuilder();

        sb.append("Error Message: ").append(ex.getMessage()).append("\n");

        Throwable root = ex;
        while (root.getCause() != null) {
            root = root.getCause();
        }

        if (root != ex) {
            sb.append("Root Cause: ").append(root).append("\n");
        }

        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 2000) break;
        }

        return sb.toString();
    }

}
