package com.august.authenticate.dto.responses.authen;

import com.august.authenticate.enums.TokenType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;

@Setter
@Builder
public class AuthenticationResponse {

    @JsonProperty("token_type")
    private TokenType tokenType;

    @JsonProperty("access_token")
    private String accessToken;
    private Long expiresIn;

}
