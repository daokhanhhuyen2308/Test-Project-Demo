package com.august.gateway.config;

import com.august.gateway.dto.responses.ApiResponse;
import com.august.gateway.dto.responses.IntrospectResponse;
import com.august.gateway.exception.ApiExceptionResponse;
import com.august.gateway.exception.CustomError;
import com.august.gateway.service.AuthenticationService;
import com.august.gateway.utils.Endpoints;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Global Pre Filter executed");

        if (checkPublicEndpoints(exchange.getRequest())){
            return chain.filter(exchange);
        }

        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

        if (!httpHeaders.containsHeader(HttpHeaders.AUTHORIZATION)){
            return unAuthorize(exchange);
        }

        String header = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);

        if (!Objects.nonNull(header)){
            return unAuthorize(exchange);
        }

        String token = header.substring(7);

        Mono<ApiResponse<IntrospectResponse>> responseMono = authenticationService.introspect(token);

        return responseMono.flatMap(value -> {
            boolean isValid = value.getResult().isValid();
            if (isValid){
                return chain.filter(exchange);
            }
            else return unAuthorize(exchange);
        });

    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean checkPublicEndpoints(ServerHttpRequest request){
        return Arrays.stream(Endpoints.Public_Endpoints)
                .anyMatch(req -> request.getURI().getPath().matches(req));

    }

    private Mono<Void> unAuthorize(ServerWebExchange exchange){
        CustomError customError = CustomError.builder()
                .code(403)
                .timestamp(Instant.now())
                .message("")
                .build();

        ApiExceptionResponse ex = ApiExceptionResponse.builder()
                .error(customError)
                .build();

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json;

        try {
            json = objectMapper.writeValueAsString(ex);
            DataBuffer dataBuffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        }
        catch (JsonParseException e){
            return Mono.error(e);
        }

    }




}
