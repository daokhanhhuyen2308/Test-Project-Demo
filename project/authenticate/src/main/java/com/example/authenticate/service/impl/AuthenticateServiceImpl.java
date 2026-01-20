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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
@RequiredArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtils jwtTokenUtils;


    @Override
    public ApiResponse<AuthenticationResponse> authenticate(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail());

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            throw CustomExceptionHandler.badRequestException("Invalid password");
        }

        String token = jwtTokenUtils.generateToken(user.getId(), user);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(AuthenticationResponse.builder()
                        .tokenType(TokenType.Bearer)
                        .accessToken(token)
                        .build())
                .build();
    }

    @Override
    public ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws ParseException, JOSEException {

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
