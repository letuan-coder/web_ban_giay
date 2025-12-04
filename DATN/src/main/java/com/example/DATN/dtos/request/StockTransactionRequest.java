package com.example.DATN.dtos.request;

import com.example.DATN.constant.TransactionType;
import com.google.api.client.util.DateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StockTransactionRequest {

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private String supplierId;
    private String fromWarehouseId;
    private String fromStoreId;
    private String toWarehouseId;
    private String toStoreId;
    private DateTime expectedReceivedDate;

    @NotNull(message = "Items list cannot be null")
    @Size(min = 1, message = "Transaction must have at least one item")
    @Valid // This annotation triggers validation for each item in the list
    private List<StockTransactionItemRequest> items;
}
