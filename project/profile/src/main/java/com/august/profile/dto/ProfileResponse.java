package com.august.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ProfileResponse {
    @JsonProperty("profile_id")
    private String profileId;
    @JsonProperty("user_id")
    private String userId;
    private String username;
    private String email;

}
