package com.august.comment.controller;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.RepliesPaginationFilter;
import com.august.comment.service.CommentService;
import com.august.comment.utils.ParseStringToObject;
import com.august.shared.dto.ApiResponse;
import com.august.shared.dto.PageResponse;
import com.august.shared.enums.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentResponse> createCommentPost(@RequestBody @Valid CommentRequest request){
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
        RepliesPaginationFilter filter = RepliesPaginationFilter.builder()
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
                                                                           @RequestParam(defaultValue = "0", required = false) int page,
                                                                           @RequestParam(defaultValue = "10", required = false) int size,
                                                                           @RequestParam(required = false) String cursor,
                                                                           @RequestParam(defaultValue = "true") Boolean sort){
        RepliesPaginationFilter filter = RepliesPaginationFilter.builder()
                .page(page)
                .size(size)
                .sortDesc(sort)
                .searchAfter(ParseStringToObject.parse(cursor))
                .build();

        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(commentService.getAllCommentsBySlug(filter, slug))
                .code(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode())
                .message(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage())
                .build();
    }
}
