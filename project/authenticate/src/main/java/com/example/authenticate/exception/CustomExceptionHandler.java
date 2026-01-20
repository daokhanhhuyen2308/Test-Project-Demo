package com.example.authenticate.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Builder
@Getter
public class CustomExceptionHandler extends RuntimeException{

    private HttpStatus status;
    private CustomError errors;

    public static CustomExceptionHandler notFoundException(String message){

        return CustomExceptionHandler.builder()
                .status(HttpStatus.NOT_FOUND)
                .errors(
                        CustomError.builder()
                                .code(HttpStatus.NOT_FOUND.value())
                                .message(message)
                                .build())
                .build();
    }

    public static CustomExceptionHandler badRequestException(String message){
        return CustomExceptionHandler.builder()
                .status(HttpStatus.BAD_REQUEST)
                .errors(CustomError.builder()
                        .message(message)
                        .code(HttpStatus.BAD_REQUEST.value())
                        .build())
                .build();
    }

    public static CustomExceptionHandler unAuthorizeException(String msg){
        return CustomExceptionHandler.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .errors(CustomError.builder()
                        .message(msg)
                        .code(HttpStatus.UNAUTHORIZED.value())
                        .build())
                .build();
    }
}
