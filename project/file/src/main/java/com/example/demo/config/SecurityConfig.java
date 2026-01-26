package com.example.demo.config;

import com.example.demo.exception.CustomAccessDeniedHandler;
import com.example.demo.exception.CustomAuthenticationEntryPoint;
import com.example.demo.utils.Endpoints;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomJwtDecoder customJwtDecoder;

    public SecurityConfig(CustomJwtDecoder customJwtDecoder) {
        this.customJwtDecoder = customJwtDecoder;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity){
        httpSecurity
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/api/file/upload", "/api/file/export/{fileId}")
                                        .authenticated());
        httpSecurity.oauth2ResourceServer(
                oauth2 ->
                        oauth2.jwt(
                                jwtConfigurer ->
                                        jwtConfigurer
                                                .decoder(customJwtDecoder)
                                                .jwtAuthenticationConverter(converter())));

        httpSecurity.exceptionHandling(
                exception -> {
                    exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
                    exception.accessDeniedHandler(new CustomAccessDeniedHandler());
                });

        httpSecurity.sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return httpSecurity.build();



    }
    @Bean
    public JwtAuthenticationConverter converter(){
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName("scope");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;

    }

}
