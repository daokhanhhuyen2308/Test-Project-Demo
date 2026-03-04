package com.august.post.controller;

import com.august.sharecore.dto.PageResponse;
import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostPaginationFilter;
import com.august.post.dto.PostResponse;
import com.august.post.service.PostService;
import com.august.sharecore.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/create")
    public ApiResponse<PostResponse> createPost(@RequestBody @Valid PostCreationRequest request){
        return postService.createPost(request);
    }

    @GetMapping("/{slug}")
    public ApiResponse<PostResponse> getPostBySlug(@PathVariable String slug){
        return postService.getPostBySlug(slug);
    }

    @GetMapping("/query")
    public ApiResponse<PageResponse<PostResponse>> searchPosts(@ModelAttribute PostPaginationFilter filter) throws IOException {
        return postService.searchPosts(filter);
    }

    @GetMapping("/{postId}/related")
    public ApiResponse<List<PostResponse>> getRelatedPosts(@PathVariable Long postId){
        ApiResponse<List<PostResponse>> response = new ApiResponse<>();
        response.setResult(postService.getRelatedPosts(postId));
        return response;
    }

    @PostMapping("/{postId}/upload-thumbnail")
    public ApiResponse<PostResponse> uploadThumbnail(@PathVariable String postId,
                                                     @RequestParam MultipartFile file){
        return ApiResponse.<PostResponse>builder()
                .result(postService.uploadThumbnail(postId, file))
                .build();
    }



}
