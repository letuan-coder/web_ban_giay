package com.example.DATN.dtos.request;

import com.example.DATN.constant.TransactionType;
import lombok.Data;

import java.util.List;

@Data
public class StockTransactionRequest {
    private TransactionType type;
    private Long supplierId;
    private Long fromWarehouseId;
    private Long fromStoreId;
    private Long toWarehouseId;
    private Long toStoreId;
    private List<StockTransactionItemRequest> items;
}
