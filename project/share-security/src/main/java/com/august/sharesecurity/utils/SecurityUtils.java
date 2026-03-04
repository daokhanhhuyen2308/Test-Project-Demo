package com.august.sharesecurity.utils;

import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.AppCustomException;
import com.august.sharesecurity.dto.AuthCurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public AuthCurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AppCustomException(ErrorCode.UNAUTHENTICATED);
        }

        System.out.println("Id " +jwt.getId());
        System.out.println("Username " +jwt.getClaimAsString("preferred_username"));
        System.out.println("Email " +jwt.getClaimAsString("email"));
        System.out.println("Avatar " +jwt.getClaimAsString("avatar"));

        return AuthCurrentUser.builder()
                .keycloakId(jwt.getSubject())
                .username(jwt.getClaimAsString("preferred_username"))
                .email(jwt.getClaimAsString("email"))
                .avatarUrl(jwt.getClaimAsString("avatar"))
                .build();
    }
}
