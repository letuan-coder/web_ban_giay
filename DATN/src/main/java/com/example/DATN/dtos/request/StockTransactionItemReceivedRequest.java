package com.example.DATN.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockTransactionItemReceivedRequest {
    @NotNull(message = "Product Variant ID is required")
    private UUID stockTransactionId;
    private Integer receivedQuantity;
    private Integer sendingQuantity;
}
