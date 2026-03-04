package com.august.comment.controller;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.CommentPaginationFilter;
import com.august.comment.service.CommentService;
import com.august.comment.utils.ParseStringToObject;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.dto.PageResponse;
import com.august.sharecore.enums.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/create-comment")
    public ApiResponse<CommentResponse> createCommentPost(@RequestBody @Valid CommentRequest request){
        System.out.println("Vào day roi ");
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createCommentPost(request))
                .build();
    }

    @GetMapping("/{parentCmtId}/replies")
    public ApiResponse<PageResponse<CommentResponse>> getAllCommentReplies(@PathVariable String parentCmtId,
                                                                           @RequestParam(defaultValue = "0", required = false) int page,
                                                                           @RequestParam(defaultValue = "10", required = false) int size,
                                                                           @RequestParam(required = false) String cursor,
                                                                           @RequestParam(defaultValue = "true") Boolean sort){
        CommentPaginationFilter filter = CommentPaginationFilter.builder()
                .page(page)
                .size(size)
                .parentCmtId(parentCmtId)
                .sortDesc(sort)
                .searchAfter(ParseStringToObject.parse(cursor))
                .build();

        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getAllCommentReplies(filter))
                .code(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode())
                .message(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage())
                .build();
    }

    @GetMapping("/{slug}")
    public ApiResponse<PageResponse<CommentResponse>> getAllCommentsBySlug(@PathVariable String slug,
                                                                           @ModelAttribute CommentPaginationFilter filter){

        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getAllCommentsBySlug(filter, slug))
                .code(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode())
                .message(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage())
                .build();
    }

    @GetMapping("/ping")
    public String ping() { return "ok"; }
}
