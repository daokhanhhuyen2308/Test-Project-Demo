package com.august.sharesecurity.config;

import com.august.sharesecurity.utils.KeycloakResourceRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class SharedJwtSecurityConfig {

    @Bean
    public KeycloakResourceRoleConverter keycloakResourceRoleConverter(){
        return new KeycloakResourceRoleConverter();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(KeycloakResourceRoleConverter customConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(customConverter);
        return converter;
    }
}
