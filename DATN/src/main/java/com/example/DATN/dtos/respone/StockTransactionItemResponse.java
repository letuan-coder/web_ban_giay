package com.example.DATN.dtos.respone;

import lombok.Data;

import java.util.UUID;

@Data
public class StockTransactionItemResponse {
    private UUID id;
    private UUID variantId;
    private String name;
    private String colorName;
    private Integer sizeName;
    private String variantSku; // For convenience
    private Integer quantity;
}
