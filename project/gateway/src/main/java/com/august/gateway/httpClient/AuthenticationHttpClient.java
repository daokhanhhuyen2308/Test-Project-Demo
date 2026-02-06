package com.august.gateway.httpClient;

import com.august.gateway.dto.requests.IntrospectRequest;
import com.august.gateway.dto.responses.IntrospectResponse;
import com.august.shared.dto.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface AuthenticationHttpClient {
    @PostExchange("/token")
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
