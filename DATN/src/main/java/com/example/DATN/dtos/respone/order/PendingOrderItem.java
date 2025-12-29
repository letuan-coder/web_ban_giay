package com.example.DATN.dtos.respone.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PendingOrderItem {
    private UUID ID;
    private String name;
    private String sku;
    private Integer weight;
    private Integer height;
    private Integer width;
    private Integer length;
    private BigDecimal price;
    private Integer quantity;
}
