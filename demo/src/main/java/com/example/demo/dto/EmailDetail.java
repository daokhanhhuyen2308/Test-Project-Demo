package com.example.demo.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDetail {
    private String recipient;
    private String msgBody;
    private String subject;
}
