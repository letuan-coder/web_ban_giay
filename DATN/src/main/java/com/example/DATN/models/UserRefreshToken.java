
package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_refresh_tokens")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRefreshToken extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expireTime;

    @Column(nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
