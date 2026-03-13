package com.august.profile.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {
    @JsonProperty("profile_id")
    private String profileId;
    private String username;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    private String bio;
    @JsonProperty("is_following")
    private Boolean isFollowing;
}
