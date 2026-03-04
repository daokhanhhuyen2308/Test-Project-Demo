package com.august.sharesecurity.endpoints;

public class PostEndpoints {

    public static final String[] PUBLIC_GET = {
            "/api/posts/query",
            "/api/posts/*",
            "/api/posts/*/related"
    };

    public static final String[] PRIVATE_POST = {
            "/api/posts/create"
    };
}