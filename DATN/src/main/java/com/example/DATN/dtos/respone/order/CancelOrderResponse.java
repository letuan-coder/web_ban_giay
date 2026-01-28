package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.RefundStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
public class CancelOrderResponse {
    private String vnpResponseCode;
    private String vnpMessage;
    private String vnpTxnRef;

    private UUID refundId;
    private RefundStatus refundStatus;
    private BigDecimal refundAmount;
    private LocalDateTime expectedRefundDate;
}
