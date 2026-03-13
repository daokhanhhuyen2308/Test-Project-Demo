package com.august.sharesecurity.endpoints;

public class PostEndpoints {

    public static final String[] PUBLIC_GET = {
            "/api/posts/query/**",
            "/api/posts/*/related",
            "/api/posts/*" //slug
    };

    public static final String[] PRIVATE_POST = {
            "/api/posts/create",
            "/api/posts/*/upload-thumbnail",
            "/api/posts/*/favorite"
    };
}