package com.example.DATN.dtos.respone;

import com.example.DATN.constant.TransactionStatus;
import com.example.DATN.constant.TransactionType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StockTransactionResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    private UUID id;
    private String code;
    private TransactionType type;
    private TransactionStatus transactionStatus;
    private UUID supplierId;
    private String supplierName;
    private UUID fromWarehouseId;
    private String fromWarehouseName;
    private UUID fromStoreId;
    private String fromStoreName;
    private UUID toWarehouseId;
    private String toWarehouseName;
    private UUID toStoreId;
    private String toStoreName;
    private LocalDateTime createdDate;
    private List<StockTransactionItemResponse> items;
}
