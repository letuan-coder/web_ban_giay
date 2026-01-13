package com.example.DATN.dtos.respone.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LockStockResult {
    boolean success;
    int lockedQuantity;

    public LockStockResult(String sku, UUID storeId, Integer quantity, Long userId) {
    }
}
