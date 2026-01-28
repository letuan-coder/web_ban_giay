package com.example.DATN.dtos.request;

import com.example.DATN.constant.RefundStatus;
import com.example.DATN.models.Order;
import com.example.DATN.models.OrderReturn;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundRequest {
    private String reason;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime expectedRefundDate;

    private LocalDateTime completedAt;

    private String refundTransactionId;

    private OrderReturn orderReturn;

    private Order order;
}
