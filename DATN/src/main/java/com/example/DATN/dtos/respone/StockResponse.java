package com.example.DATN.dtos.respone;

import com.example.DATN.constant.StockType;
import lombok.Data;

import java.util.UUID;

@Data
public class StockResponse {
    private UUID id;
    private StockType stockType;
    private UUID warehouseId;
    private UUID storeId;
    private String sku;
    private Integer quantity;
//    private LocalDate actualReceivedDate;
}
