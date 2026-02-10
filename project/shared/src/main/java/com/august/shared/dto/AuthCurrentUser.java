package com.august.shared.dto;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthCurrentUser {
    private String userId;
    private String username;
    private String email;
    private String avatarUrl;
}
