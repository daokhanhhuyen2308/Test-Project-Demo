package com.example.authenticate.controller;

import com.example.authenticate.dto.requests.IntrospectRequest;
import com.example.authenticate.dto.requests.LoginRequest;
import com.example.authenticate.dto.responses.ApiResponse;
import com.example.authenticate.dto.responses.IntrospectResponse;
import com.example.authenticate.dto.responses.authen.AuthenticationResponse;
import com.example.authenticate.service.AuthenticateService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticateController {
    private final AuthenticateService authenticateService;

    @PostMapping("/token")
    public Mono<ApiResponse<AuthenticationResponse>> authenticate(@Valid @RequestBody LoginRequest request){
        return authenticateService.authenticate(request);
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return authenticateService.introspect(request);
    }

}
