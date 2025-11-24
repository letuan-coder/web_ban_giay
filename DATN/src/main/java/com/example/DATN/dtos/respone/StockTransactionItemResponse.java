package com.example.DATN.dtos.respone;

import lombok.Data;

@Data
public class StockTransactionItemResponse {
    private Long id;
    private Long variantId;
    private String variantSku; // For convenience
    private Integer quantity;
}
