package com.august.authenticate.service;

import com.august.authenticate.dto.requests.IntrospectRequest;
import com.august.authenticate.dto.requests.LoginRequest;
import com.august.authenticate.dto.responses.IntrospectResponse;
import com.august.authenticate.dto.responses.authen.AuthenticationResponse;
import com.august.shared.dto.ApiResponse;
import com.nimbusds.jose.JOSEException;
import reactor.core.publisher.Mono;

import java.text.ParseException;

public interface AuthenticateService {
    ApiResponse<AuthenticationResponse> authenticate(LoginRequest request);

    ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws ParseException, JOSEException;
}
