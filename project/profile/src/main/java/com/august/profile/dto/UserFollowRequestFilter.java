package com.august.profile.dto;

import com.august.sharecore.dto.SearchAfterCursor;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserFollowRequestFilter {
    private String authorId;
    private String currentKeycloakId;
    private int page = 0;
    private int size = 20;
    private SearchAfterCursor searchAfterCursor;
}
