package com.august.sharecore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String message;
    private Instant timestamp;
    private int status;
    private T result;
    private String trace;

    public static <T> ApiResponse<T> success(T result, String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .code("1000")
                .message(message)
                .result(result)
                .build();
    }

}
