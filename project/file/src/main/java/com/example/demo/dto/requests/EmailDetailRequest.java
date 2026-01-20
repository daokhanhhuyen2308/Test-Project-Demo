package com.example.demo.dto.requests;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDetailRequest {
    @NotBlank(message = "Recipient cannot be empty")
    @Email(message = "Invalid email format")
    private String recipient;
    @NotBlank(message = "Message body cannot be empty")
    private String msgBody;
    @NotBlank(message = "Subject cannot be empty")
    private String subject;
}
