package com.example.authenticate.exception;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExceptionResponse {
    private CustomError error;
}
