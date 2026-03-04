package com.august.sharesecurity.dto;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthCurrentUser {
    private String keycloakId;
    private String username;
    private String email;
    private String avatarUrl;
}
