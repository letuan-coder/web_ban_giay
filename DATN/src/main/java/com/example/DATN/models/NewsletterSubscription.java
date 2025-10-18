package com.example.DATN.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "newsletter_subscriptions")
public class NewsletterSubscription extends BaseEntity {
    @Id
    @Column(unique = true, nullable = false)
    @Email(message = "EMAIL_INVALID")
    private String email;

    @Column(nullable = false)
    private boolean isActive;
}
