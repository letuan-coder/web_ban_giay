package com.example.DATN.dtos.respone.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemRespone {
    private Long id;
    private UUID productColorId;
    private Integer quantity;
    private BigDecimal price;
}
