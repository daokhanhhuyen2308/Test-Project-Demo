package com.example.authenticate.config;

import com.example.authenticate.dto.requests.IntrospectRequest;
import com.example.authenticate.dto.responses.IntrospectResponse;
import com.example.authenticate.exception.CustomExceptionHandler;
import com.example.authenticate.service.AuthenticateService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final AuthenticateService authenticateService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;
    @Value("${token.secret.key}")
    private String SIGNER_KEY;

    public CustomJwtDecoder(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        IntrospectRequest request = IntrospectRequest.builder()
                .token(token)
                .build();

        try {
            IntrospectResponse response = authenticateService.introspect(request).getResult();

            if (!response.isValid()){
                throw CustomExceptionHandler.badRequestException("Invalid token");
            }

        } catch (ParseException | JOSEException e) {
            throw new RuntimeException(e);
        }

        if (Objects.isNull(nimbusJwtDecoder)){
            SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
