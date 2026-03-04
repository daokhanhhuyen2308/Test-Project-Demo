package com.august.comment.service;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.CommentPaginationFilter;
import com.august.sharecore.dto.PageResponse;

public interface CommentService {
    CommentResponse createCommentPost(CommentRequest request);

    PageResponse<CommentResponse> getAllCommentReplies(CommentPaginationFilter filter);

    PageResponse<CommentResponse> getAllCommentsBySlug(CommentPaginationFilter filter, String slug);
}
