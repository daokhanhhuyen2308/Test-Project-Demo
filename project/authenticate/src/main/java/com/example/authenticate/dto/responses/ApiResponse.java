package com.example.authenticate.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class ApiResponse<T> {
    private int code;
    private T result;

}
