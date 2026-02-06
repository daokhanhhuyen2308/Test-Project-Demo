package com.august.authenticate.controller;

import com.august.authenticate.dto.requests.IntrospectRequest;
import com.august.authenticate.dto.requests.LoginRequest;
import com.august.authenticate.dto.responses.IntrospectResponse;
import com.august.authenticate.dto.responses.authen.AuthenticationResponse;
import com.august.authenticate.service.AuthenticateService;
import com.august.shared.dto.ApiResponse;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticateController {
    private final AuthenticateService authenticateService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@Valid @RequestBody LoginRequest request){
        return authenticateService.authenticate(request);
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return authenticateService.introspect(request);
    }

}
