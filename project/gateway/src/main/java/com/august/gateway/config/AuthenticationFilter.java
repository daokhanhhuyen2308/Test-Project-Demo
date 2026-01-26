package com.august.gateway.config;

import com.august.gateway.dto.responses.ApiResponse;
import com.august.gateway.dto.responses.IntrospectResponse;
import com.august.gateway.exception.ApiExceptionResponse;
import com.august.gateway.exception.CustomError;
import com.august.gateway.exception.CustomExceptionHandler;
import com.august.gateway.service.AuthenticationService;
import com.august.gateway.utils.Endpoints;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;
    final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final int MAX_REQUESTS_ALLOWED = 20;
    private final int DURATION_ALLOWED = 30;
    private final int MAX_LOGIN_FAILED_ALLOWED = 5;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @NonNull
    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull GatewayFilterChain chain) {
        logger.info("Global Pre Filter executed");

        if (exchange.getRequest().getURI().getPath().equals("/api/auth/token")){
            return handleCheckRateLimitAndLoginAttempts(exchange)
                    .then(Mono.defer(() -> chain.filter(exchange)));
        }
        if (checkPublicEndpoints(exchange.getRequest())){
            return chain.filter(exchange);
        }

        HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

        if (!httpHeaders.containsHeader(HttpHeaders.AUTHORIZATION)){
            return unAuthorize(exchange);
        }

        String authHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);

        if (!Objects.nonNull(authHeader)){
            return unAuthorize(exchange);
        }

        String token = authHeader.substring(7);

        Mono<ApiResponse<IntrospectResponse>> responseMono = authenticationService.introspect(token);

        return responseMono.flatMap(value -> {
            boolean isValid = value.getResult().isValid();
            if (isValid){

                ServerWebExchange mutateExchange = exchange.mutate()
                        .request(r -> r.header(HttpHeaders.AUTHORIZATION, "Bearer " +token))
                        .build();

                return chain.filter(mutateExchange);
            }
            else return unAuthorize(exchange);
        });

    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> handleCheckRateLimitAndLoginAttempts(ServerWebExchange exchange){
        return extractEmailFromRequest(exchange)
                .flatMap(email -> {
                    String rateLimitRequestKey = "rateLimit:attempt:" + email;
                    String loginAttemptKey = "login:attempt:" +email;

                    return checkRateLimitAttempts(rateLimitRequestKey, exchange)
                            .then(checkLoginAttempts(loginAttemptKey, exchange));
                });

    }

    private Mono<Void> checkRateLimitAttempts(String rateLimitKey, ServerWebExchange exchange){
        return reactiveRedisTemplate.opsForValue().increment(rateLimitKey)
                .flatMap(countLimit -> {
                    if (countLimit == 1){
                        return reactiveRedisTemplate.expire(rateLimitKey, Duration.ofSeconds(DURATION_ALLOWED));
                    }
                    if (countLimit > MAX_REQUESTS_ALLOWED){
                        return reactiveRedisTemplate.getExpire(rateLimitKey).flatMap(remainingTimeLimitTTl -> {
                            Duration ttl = remainingTimeLimitTTl.isNegative() || remainingTimeLimitTTl.isZero()
                                    ? Duration.ofSeconds(DURATION_ALLOWED) :  remainingTimeLimitTTl;
                            return tooManyRequest(exchange, ttl);
                        });
                    }
                    return Mono.empty();

                }).then();
    }

    private Mono<Void> checkLoginAttempts(String loginAttemptKey, ServerWebExchange exchange){
        return reactiveRedisTemplate.opsForValue().get(loginAttemptKey)
                .defaultIfEmpty("0")
                .flatMap(login -> {
                    int countLogin = Integer.parseInt(login);
                    if (countLogin >= MAX_LOGIN_FAILED_ALLOWED) return unAuthorize(exchange);
                    return Mono.empty();
                });
    }

    private Mono<String> extractEmailFromRequest(ServerWebExchange exchange){
        return exchange.getRequest().getBody().next().flatMap(
                dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    try {
                        JsonNode jsonNode = objectMapper.readTree(bytes);
                        String email = jsonNode.get("email").asString();

                        if (email == null) {
                            return Mono.error(CustomExceptionHandler.notFoundException("Email empty in body"));
                        }

                        return Mono.just(email);

                    } catch (Exception e) {
                        return Mono.error(CustomExceptionHandler.badRequestException("Invalid JSON"));
                    }
                }).switchIfEmpty(Mono.error(CustomExceptionHandler.badRequestException("Body is missing")));

    }


    private boolean checkPublicEndpoints(ServerHttpRequest request){
        return Arrays.stream(Endpoints.Public_Endpoints)
                .anyMatch(req -> request.getURI().getPath().matches(req));

    }

    private Mono<Void> handleException(ServerWebExchange exchange, int code, String msg){
        CustomError customError = CustomError.builder()
                .code(code)
                .timestamp(Instant.now())
                .message(msg)
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

    private Mono<Void> unAuthorize(ServerWebExchange exchange){
        return handleException(exchange, 403, "Access Denied: You do not have the necessary permissions to perform this action.");
    }

    private Mono<Void> tooManyRequest(ServerWebExchange exchange, Duration duration){
        long seconds = duration.getSeconds();
        String msg = "Rate limit exceeded. Please try again in %d seconds." + seconds;
        exchange.getResponse().getHeaders().set("Retry-After", msg);
        return handleException(exchange, 429, msg);
    }

}
