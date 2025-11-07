package com.example.DATN.dtos.request.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemRequest {
    private UUID productVariantId;
    private Integer quantity;
    private BigDecimal price;
}