package com.august.notification.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDetailRequest {
    private String recipient;
    private String subject;
    private String msgBody;
}
