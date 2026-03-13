package com.august.comment.controller;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.CommentPaginationFilter;
import com.august.comment.service.CommentService;
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
        return ApiResponse.success(commentService.createCommentPost(request), "Create comment successfully");
    }

    @GetMapping("/{parentCmtId}/replies")
    public ApiResponse<PageResponse<CommentResponse>> getAllCommentReplies(@PathVariable String parentCmtId,
                                                                           @ModelAttribute CommentPaginationFilter filter){

        filter.setParentCmtId(parentCmtId);

        return ApiResponse.success(commentService.getAllCommentReplies(filter),
                ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());
    }

    @GetMapping("/{postId}")
    public ApiResponse<PageResponse<CommentResponse>> getAllCommentsByPostId(@PathVariable Long postId,
                                                                           @ModelAttribute CommentPaginationFilter filter){

        return ApiResponse.success(commentService.getAllCommentsByPostId(filter, postId),
                ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());

    }
}
