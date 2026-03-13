package com.august.comment.dto;

import com.august.sharecore.dto.SearchAfterCursor;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
public class CommentPaginationFilter {
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int size = 10;
    private String parentCmtId;
    @Builder.Default
    private boolean sortDesc = true;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
    private SearchAfterCursor searchAfter;
    public int getPage() {
        return Math.max(0, page - 1);
    }
    public int getSize() {
        return size > 0 ? size : 10;
    }
}
