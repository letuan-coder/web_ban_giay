package com.example.DATN.dtos.respone;

import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StockTransactionResponse {
    private Long id;
    private TransactionType type;
    private TransactionStatus transactionStatus;
    private Long supplierId;
    private String supplierName;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private Long fromStoreId;
    private String fromStoreName;
    private Long toWarehouseId;
    private String toWarehouseName;
    private Long toStoreId;
    private String toStoreName;
    private LocalDateTime createdDate;
    private List<StockTransactionItemResponse> items;
}
