package com.example.DATN.dtos.request;

import com.example.DATN.constant.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StockTransactionRequest {

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private Long supplierId;
    private Long fromWarehouseId;
    private Long fromStoreId;
    private Long toWarehouseId;
    private Long toStoreId;

    @NotNull(message = "Items list cannot be null")
    @Size(min = 1, message = "Transaction must have at least one item")
    @Valid // This annotation triggers validation for each item in the list
    private List<StockTransactionItemRequest> items;
}
