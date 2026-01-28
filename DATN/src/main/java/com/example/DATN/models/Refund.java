package com.example.DATN.models;

import com.example.DATN.constant.RefundStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refunds")
public class Refund extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;

    private String reason;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime expectedRefundDate;

    private LocalDateTime completedAt;

    private String refundTransactionId;
    @ManyToOne
    @JoinColumn(name="order_return_id")
    private OrderReturn orderReturn;

    // Liên kết đến Order gốc
    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;
}
