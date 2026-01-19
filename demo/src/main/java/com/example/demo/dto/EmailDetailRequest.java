package com.example.demo.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDetailRequest {
    private String recipient;
    private String msgBody;
    private String subject;
}
