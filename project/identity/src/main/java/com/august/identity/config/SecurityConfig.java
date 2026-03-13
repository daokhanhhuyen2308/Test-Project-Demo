package com.august.identity.config;

import com.august.identity.utils.Endpoints;
import com.august.sharesecurity.exception.CustomAccessDenied;
import com.august.sharesecurity.exception.CustomAuthEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CustomAuthEntryPoint customAuthEntryPoint(ObjectMapper objectMapper) {
        return new CustomAuthEntryPoint(objectMapper);
    }

    @Bean
    public CustomAccessDenied customAccessDenied(ObjectMapper objectMapper) {
        return new CustomAccessDenied(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   CustomAccessDenied accessDenied,
                                                   CustomAuthEntryPoint authEntryPoint) throws Exception {
        httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(Endpoints.PUBLIC_ENDPOINTS)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.POST, "/api/users/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated());
        httpSecurity.oauth2ResourceServer(
                oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        httpSecurity.exceptionHandling(
                exception -> {
                    exception.authenticationEntryPoint(authEntryPoint);
                    exception.accessDeniedHandler(accessDenied);
                });

        httpSecurity.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return httpSecurity.build();
    }
}
