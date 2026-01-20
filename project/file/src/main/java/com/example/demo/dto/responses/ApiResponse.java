package com.example.demo.dto.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse<T> {
    private int code;
    private T result;

}
