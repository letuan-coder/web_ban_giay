package com.example.DATN.dtos.respone;

import com.example.DATN.constant.StockType;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class StockResponse {
    private UUID id;
    private String sku;
    private StockType stockType;
    private UUID warehouseId;
    private UUID storeId;
    private Integer quantity;
    private LocalDate actualReceivedDate;
}
