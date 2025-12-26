package com.example.DATN.dtos.respone.order;

import lombok.Data;

@Data
public class PendingOrderItem {
    private String sku;
    private Integer quantity;
}
