package com.august.gateway.service;

import com.august.gateway.dto.requests.IntrospectRequest;
import com.august.gateway.dto.responses.IntrospectResponse;
import com.august.gateway.httpClient.AuthenticationHttpClient;
import com.august.shared.dto.ApiResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthenticationService {
    private final AuthenticationHttpClient httpClient;

    public AuthenticationService(AuthenticationHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Mono<ApiResponse<IntrospectResponse>> introspect(String token){
        IntrospectRequest request = IntrospectRequest.builder()
                .token(token)
                .build();
        return httpClient.introspect(request);
    }


}
