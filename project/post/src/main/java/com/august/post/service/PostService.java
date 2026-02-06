package com.august.post.service;

import com.august.post.dto.PageResponse;
import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostPaginationFilter;
import com.august.post.dto.PostResponse;
import com.august.shared.dto.ApiResponse;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.util.List;


public interface PostService {
    ApiResponse<PostResponse> createPost(PostCreationRequest request, Jwt jwt);

    ApiResponse<PageResponse<PostResponse>> searchPosts(PostPaginationFilter filter) throws IOException;

    ApiResponse<PostResponse> getPostBySlug(String slug);

    List<PostResponse> getRelatedPosts(Long postId);
}
