package com.example.DATN.dtos.respone;

import com.example.DATN.constant.StockType;
import lombok.Data;

import java.util.UUID;

@Data
public class StockResponse {
    private Long id;
    private UUID variantId;
    private StockType stockType;
    private Long warehouseId;
    private Long storeId;
    private Integer quantity;
}
