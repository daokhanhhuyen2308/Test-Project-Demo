package com.august.sharesecurity.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class KeycloakResourceRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Value("${KEYCLOAK_RESOURCE_ID:web-app-demo}")
    private String resourceId;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // 1. get map resource_access
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) return Collections.emptySet();

        // 2. get data of the specific client  (eg: web-app-demo)
        Object clientData = resourceAccess.get(resourceId);
        if (!(clientData instanceof Map<?, ?> clientMap)) return Collections.emptySet();

        // 3. get list roles from that client
        Object rolesObj = clientMap.get("roles");
        if (!(rolesObj instanceof Collection<?> roles)) return Collections.emptySet();

        // 4. Map each role String to GrantedAuthority
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                .collect(Collectors.toSet());
    }
}
