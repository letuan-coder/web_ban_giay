
package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriptionResponse {
    private Long id;
    private String email;
    private LocalDateTime createdAt;
}
