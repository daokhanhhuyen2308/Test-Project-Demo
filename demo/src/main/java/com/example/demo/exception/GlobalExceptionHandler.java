package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionResponse> handleGeneralException(Exception exception,
                                                                       HttpServletRequest httpServletRequest){

        ApiExceptionResponse exceptionResponse = ApiExceptionResponse.builder()
                .error(CustomError.builder()
                        .code(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .path(httpServletRequest.getRequestURI())
                        .timestamp(Instant.now())
                        .message(exception.getMessage())
                        .build())
                .build();


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);

    }


    @ExceptionHandler(CustomExceptionHandler.class)
    public ResponseEntity<ApiExceptionResponse> customHandleException(CustomExceptionHandler exception,
                                                                      HttpServletRequest httpServletRequest){
        CustomError customError = exception.getErrors();
        customError.setPath(httpServletRequest.getRequestURI());
        customError.setTimestamp(Instant.now());
        HttpStatus status = exception.getStatus();

        ApiExceptionResponse apiHandleResponse = ApiExceptionResponse.builder()
                .error(customError)
                .build();

        return ResponseEntity.status(status).body(apiHandleResponse);

    }
}
