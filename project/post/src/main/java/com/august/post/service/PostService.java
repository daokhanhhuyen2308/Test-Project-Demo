package com.august.post.service;

import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostPaginationFilter;
import com.august.post.dto.PostResponse;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.dto.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface PostService {
    ApiResponse<PostResponse> createPost(PostCreationRequest request);

    ApiResponse<PageResponse<PostResponse>> searchPosts(PostPaginationFilter filter) throws IOException;

    ApiResponse<PostResponse> getPostBySlug(String slug);

    List<PostResponse> getRelatedPosts(Long postId);

    PostResponse uploadThumbnail(String postId, MultipartFile file);
}
