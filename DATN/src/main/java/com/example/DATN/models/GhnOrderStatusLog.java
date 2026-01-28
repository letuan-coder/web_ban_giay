package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ghn_order_status_log")
public class GhnOrderStatusLog {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private String ghnOrderCode;

    private String status;
    private String previousStatus;
    private String statusText;

    private LocalDateTime ghnUpdatedAt;
    private LocalDateTime syncedAt;

    @Column(columnDefinition = "TEXT")
    private String rawData;

    private LocalDateTime createdAt;
}
