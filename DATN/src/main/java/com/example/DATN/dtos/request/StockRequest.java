package com.example.DATN.dtos.request;

import com.example.DATN.constant.StockType;
import lombok.Data;

import java.util.UUID;

@Data
public class StockRequest {
    private UUID variantId;
    private StockType stockType;
    private Long warehouseId;
    private Long storeId;
    private Integer quantity;
    private Long stock_transaction_id;
}
