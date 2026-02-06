package com.august.authenticate.service.impl;

import com.august.authenticate.dto.requests.IntrospectRequest;
import com.august.authenticate.dto.requests.LoginRequest;
import com.august.authenticate.dto.responses.IntrospectResponse;
import com.august.authenticate.dto.responses.authen.AuthenticationResponse;
import com.august.authenticate.entity.UserEntity;
import com.august.authenticate.enums.TokenType;
import com.august.authenticate.repository.UserRepository;
import com.august.authenticate.service.AuthenticateService;
import com.august.authenticate.utils.JwtTokenUtils;
import com.august.shared.dto.ApiResponse;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final RedisTemplate<String, String> redisTemplate;
    @Value("${MAX_LOGIN_FAILED_ATTEMPTS}")
    private int MAX_LOGIN_FAILED_ATTEMPTS;

    @Value("${LOCK_DURATION}")
    private long LOCK_DURATION;


    @Override
    public ApiResponse<AuthenticationResponse> authenticate(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppCustomException(ErrorCode.EMAIL_NOT_FOUND));
        String loginAttemptKey = "LOGIN:ATTEMPT:" + user.getEmail();
        boolean isAuthenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isAuthenticated){
            redisTemplate.opsForValue().increment(loginAttemptKey);
        }
        String attemptLoginString = redisTemplate.opsForValue().get(loginAttemptKey);
        int attemptLoginCount = attemptLoginString != null ? Integer.parseInt(attemptLoginString) : 0;

        if (attemptLoginCount == 1){
            redisTemplate.expire(loginAttemptKey, Duration.ofMinutes(LOCK_DURATION));
        }
        if (attemptLoginCount >= MAX_LOGIN_FAILED_ATTEMPTS){
            throw new AppCustomException(ErrorCode.UNAUTHORIZED_PASSWORD_INVALID);
        }
                    ApiResponse<AuthenticationResponse> response = new ApiResponse<>();
                    response.setCode(ErrorCode.DATA_RESPONSE_SUCCESSFULLY.getCode());
                    response.setResult(AuthenticationResponse.builder()
                            .tokenType(TokenType.Bearer)
                            .accessToken(jwtTokenUtils.generateToken(user.getUsername(), user))
                            .build());

                    return response;

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

        ApiResponse<IntrospectResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(introspectResponse);

        return apiResponse;
    }

}
