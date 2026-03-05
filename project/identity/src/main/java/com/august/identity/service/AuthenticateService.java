package com.august.identity.service;

import com.august.identity.dto.requests.IntrospectRequest;
import com.august.identity.dto.requests.LoginRequest;
import com.august.identity.dto.responses.IntrospectResponse;
import com.august.identity.dto.responses.authen.AuthenticationResponse;
import com.august.sharecore.dto.ApiResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticateService {
    ApiResponse<AuthenticationResponse> authenticate(LoginRequest request);

    ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws ParseException, JOSEException;
}
