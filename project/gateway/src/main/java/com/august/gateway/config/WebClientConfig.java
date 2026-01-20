package com.august.gateway.config;

import com.august.gateway.httpClient.AuthenticationHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient externalServiceWebClient(){
        return WebClient.builder()
                .baseUrl("http://localhost:8081/api/auth")
                .build();
    }


    @Bean
    public AuthenticationHttpClient authenticationHttpClient(WebClient authenticationWebClient){
        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(authenticationWebClient))
                .build()
                .createClient(AuthenticationHttpClient.class);
    }
}
