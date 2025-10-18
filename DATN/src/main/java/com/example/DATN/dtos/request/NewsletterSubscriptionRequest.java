
package com.example.DATN.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriptionRequest {
    @NotEmpty(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL_FORMAT")
    private String email;
}
