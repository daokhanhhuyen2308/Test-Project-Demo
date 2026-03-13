package com.august.post.controller;

import com.august.sharecore.dto.PageResponse;
import com.august.post.dto.PostCreationRequest;
import com.august.post.dto.PostPaginationFilter;
import com.august.post.dto.PostResponse;
import com.august.post.service.PostService;
import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.enums.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    public ApiResponse<PageResponse<PostResponse>> searchPosts(@ModelAttribute PostPaginationFilter filter)
            throws IOException {
        return ApiResponse.success(postService.searchPosts(filter),
                ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getMessage());
    }

    @GetMapping("/{postId}/related")
    public ApiResponse<List<PostResponse>> getRelatedPosts(@PathVariable Long postId){
        ApiResponse<List<PostResponse>> response = new ApiResponse<>();
        response.setResult(postService.getRelatedPosts(postId));
        return ApiResponse.success(postService.getRelatedPosts(postId),
                "Retrieve successfully the related post by postId" + postId);
    }

    @PostMapping(value = "/{postId}/upload-thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> uploadThumbnail(@PathVariable Long postId,
                                                     @RequestParam MultipartFile thumbnail){
        return ApiResponse.success(postService.uploadThumbnail(postId, thumbnail),
                "Upload thumbnail successfully");
    }

    @PostMapping("/{postId}/favorite")
    public ApiResponse<PostResponse> toggleFavorite(@PathVariable Long postId){
        PostResponse postResponse = postService.toggleFavorite(postId);
        return ApiResponse.success(postResponse, postResponse.getIsFavorited() ?
                "You favorited this post" : "You unfavorited this post");

    }

}
