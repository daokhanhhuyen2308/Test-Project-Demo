package com.august.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponse {
    @JsonProperty("profile_id")
    private String profileId;
    private String username;
    private String email;
    private String avatarUrl;
    @JsonProperty("follower_count")
    private Long followerCount;
    @JsonProperty("following_count")
    private Long followingCount;
    @JsonProperty("is_following")
    private Boolean isFollowing;
    private String bio;
    @JsonProperty("is_me")
    private Boolean isMe;
}
