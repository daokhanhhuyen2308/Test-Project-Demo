package com.example.demo.exception;

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
                                .code(404)
                                .message(message)
                                .build())
                .build();
    }

    public static CustomExceptionHandler badRequestException(String message){
        return CustomExceptionHandler.builder()
                .status(HttpStatus.BAD_REQUEST)
                .errors(CustomError.builder()
                        .message(message)
                        .code(401)
                        .build())
                .build();
    }
}
