package com.august.post.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostPaginationFilter {
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int size = 10;
    private String keyword;
    private String authorUsername;
    @Builder.Default
    private boolean sortDesc = true;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    private Object[] searchAfter;
    public int getPage() {
        return Math.max(0, page - 1);
    }
    public int getSize() {
        return size > 0 ? size : 10;
    }
}
