package com.example.authenticate.service.impl;

import com.example.authenticate.dto.requests.IntrospectRequest;
import com.example.authenticate.dto.requests.LoginRequest;
import com.example.authenticate.dto.responses.ApiResponse;
import com.example.authenticate.dto.responses.IntrospectResponse;
import com.example.authenticate.dto.responses.authen.AuthenticationResponse;
import com.example.authenticate.entity.UserEntity;
import com.example.authenticate.enums.TokenType;
import com.example.authenticate.exception.CustomExceptionHandler;
import com.example.authenticate.repository.UserRepository;
import com.example.authenticate.service.AuthenticateService;
import com.example.authenticate.utils.JwtTokenUtils;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.ParseException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    @Value("${MAX_LOGIN_FAILED_ATTEMPTS}")
    private int MAX_LOGIN_FAILED_ATTEMPTS;

    @Value("${LOCK_DURATION}")
    private long LOCK_DURATION;

    @Override
    public Mono<ApiResponse<AuthenticationResponse>> authenticate(LoginRequest request) {
        return Mono.fromCallable(() -> userRepository.findByEmail(request.getEmail()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(userOptional -> {
                    if (userOptional.isEmpty()) {
                        return Mono.error(CustomExceptionHandler.notFoundException("User not found"));
                    }
                        UserEntity user = userOptional.get();
                        String loginAttemptKey = "login:attempt:" + user.getEmail();
                        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

                        if (!isAuthenticated){
                        return reactiveRedisTemplate.opsForValue().increment(loginAttemptKey)
                                .flatMap(attemptLoginCount -> {
                                    if (attemptLoginCount == 1){
                                        return reactiveRedisTemplate.expire(loginAttemptKey, Duration.ofMinutes(LOCK_DURATION))
                                                .then(Mono.error(CustomExceptionHandler.unAuthorizeException("Invalid Password")));
                                    }
                                    if (attemptLoginCount >= MAX_LOGIN_FAILED_ATTEMPTS){
                                        return Mono.error(CustomExceptionHandler.unAuthorizeException("Unfortunately!" +
                                            " You must wait five minutes cause that system is locked temporarily."));
                                    }
                                    return Mono.error(CustomExceptionHandler.unAuthorizeException("Invalid Password"));
                                });
                        }
                            String token = jwtTokenUtils.generateToken(user.getEmail(), user);

                                    ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                                            .code(200)
                                            .result(AuthenticationResponse.builder()
                                                    .tokenType(TokenType.Bearer)
                                                    .accessToken(token)
                                                    .build())
                                            .build();

                                    return reactiveRedisTemplate.delete(loginAttemptKey)
                                            .then(Mono.just(response));
                });
    }

    @Override
    public ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws ParseException {

        String token = request.getToken();

        boolean isValid = true;

        try {
            jwtTokenUtils.verifyJWT(token);
        } catch (JOSEException e) {
            isValid = false;
        }

        IntrospectResponse introspectResponse = IntrospectResponse.builder()
                .isValid(isValid)
                .build();

        return ApiResponse.<IntrospectResponse>builder()
                .result(introspectResponse)
                .build();
    }

}
