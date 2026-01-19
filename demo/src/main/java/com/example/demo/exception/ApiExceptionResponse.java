package com.example.demo.exception;

import lombok.*;

@Getter
@Setter
@Builder
public class ApiExceptionResponse {
    private CustomError error;
}
