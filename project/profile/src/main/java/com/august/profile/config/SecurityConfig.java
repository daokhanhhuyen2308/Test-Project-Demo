package com.august.profile.config;

import com.august.sharesecurity.exception.CustomAccessDenied;
import com.august.sharesecurity.exception.CustomAuthEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
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
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                       CustomAccessDenied accessDenied,
                                                       CustomAuthEntryPoint authEntryPoint) throws Exception {
                httpSecurity
                                .cors(AbstractHttpConfigurer::disable)
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(
                                                auth -> auth.anyRequest()
                                                                .authenticated());
                httpSecurity.oauth2ResourceServer(
                                oauth2 -> oauth2.jwt(
                                                jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(converter())));

                httpSecurity.exceptionHandling(
                                exception -> {
                                        exception.authenticationEntryPoint(authEntryPoint);
                                        exception.accessDeniedHandler(accessDenied);
                                });

                httpSecurity.sessionManagement(
                                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                return httpSecurity.build();

        }

        @Bean
        public JwtAuthenticationConverter converter() {
                JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
                authoritiesConverter.setAuthorityPrefix("ROLE_");
                authoritiesConverter.setAuthoritiesClaimName("scope");
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
                return converter;

        }

}
