package com.example.demo.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class CustomError {
    private String code;
    private String message;
    private Instant timestamp;
    private String path;
}
