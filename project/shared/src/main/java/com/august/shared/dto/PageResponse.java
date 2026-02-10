package com.august.shared.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder
public class PageResponse<T> {
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<T> content;
    private Object[] nextSearchAfter;
}
