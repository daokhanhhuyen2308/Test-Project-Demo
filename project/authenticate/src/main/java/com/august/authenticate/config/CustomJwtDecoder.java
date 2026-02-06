package com.august.authenticate.config;

import com.august.authenticate.dto.requests.IntrospectRequest;
import com.august.authenticate.dto.responses.IntrospectResponse;
import com.august.authenticate.service.AuthenticateService;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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

    @Lazy
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
                throw new AppCustomException(ErrorCode.INVALID_TOKEN);
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
