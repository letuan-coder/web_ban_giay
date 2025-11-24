package com.example.DATN.dtos.request;

import lombok.Data;

import java.util.UUID;

@Data
public class StockTransactionItemRequest {
    private UUID variantId;
    private Integer quantity;
}
