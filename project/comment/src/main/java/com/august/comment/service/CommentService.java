package com.august.comment.service;

import com.august.comment.dto.CommentRequest;
import com.august.comment.dto.CommentResponse;
import com.august.comment.dto.RepliesPaginationFilter;
import com.august.shared.dto.PageResponse;

public interface CommentService {
    CommentResponse createCommentPost(CommentRequest request);

    PageResponse<CommentResponse> getAllCommentReplies(RepliesPaginationFilter filter);

    PageResponse<CommentResponse> getAllCommentsBySlug(RepliesPaginationFilter filter, String slug);
}
