package com.example.authenticate.service;

import com.example.authenticate.dto.requests.IntrospectRequest;
import com.example.authenticate.dto.requests.LoginRequest;
import com.example.authenticate.dto.responses.ApiResponse;
import com.example.authenticate.dto.responses.IntrospectResponse;
import com.example.authenticate.dto.responses.authen.AuthenticationResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticateService {
    ApiResponse<AuthenticationResponse> authenticate(LoginRequest request);

    ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws ParseException, JOSEException;
}
