package com.august.authenticate.utils;

import com.august.authenticate.entity.UserEntity;
import com.august.authenticate.repository.UserRepository;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Component
public class JwtTokenUtils {

    @Value("${token.secret.key}")
    private String SIGNER_KEY;

    @Value("${access.token.expired}")
    private long expiration;

    public JwtTokenUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final UserRepository userRepository;

     public String generateToken(String subject, UserEntity user){
         JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

         String jwtId = UUID.randomUUID().toString();

         JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                 .subject(subject)
                 .issuer("augustine")
                 .jwtID(jwtId)
                 .issueTime(Date.from(Instant.now()))
                 .claim("scope", buildScope(user))
                 .expirationTime(new Date(System.currentTimeMillis() + expiration))
                 .build();

         Payload payload = new Payload(jwtClaimsSet.toJSONObject());

         JWSObject jwsObject = new JWSObject(jwsHeader, payload);

         try {
             jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
             return jwsObject.serialize();
         } catch (JOSEException e) {
             throw new RuntimeException(e);
         }
     }

    public void verifyJWT(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryToken = signedJWT.getJWTClaimsSet().getExpirationTime();

        String id = signedJWT.getJWTClaimsSet().getSubject();

        if (!signedJWT.verify(verifier) || expiryToken.after(new Date()) || !userRepository.existsById(id)){
            throw new AppCustomException(ErrorCode.INVALID_TOKEN);

        }

    }


     private String buildScope(UserEntity user){
         StringJoiner stringJoiner = new StringJoiner(" ");

         if (user.getRoles() != null) {
             user.getRoles()
                     .forEach(
                             role ->
                                 stringJoiner.add("ROLE_" + role.getName()));
         }
         return stringJoiner.toString();
     }
}
