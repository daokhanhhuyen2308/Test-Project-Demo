package com.august.shared.utils;

import com.august.shared.dto.AuthCurrentUser;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    public AuthCurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AppCustomException(ErrorCode.UNAUTHENTICATED);
        }

        return AuthCurrentUser.builder()
                .userId(jwt.getSubject())
                .username(jwt.getClaimAsString("preferred_username"))
                .email(jwt.getClaimAsString("email"))
                .avatarUrl(jwt.getClaimAsString("avatar"))
                .build();
    }
}
