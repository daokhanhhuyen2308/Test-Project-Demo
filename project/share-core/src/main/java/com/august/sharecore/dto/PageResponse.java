package com.august.sharecore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private List<T> content;
    private boolean hasMore;
    private SearchAfterCursor nextSearchAfter;
}
