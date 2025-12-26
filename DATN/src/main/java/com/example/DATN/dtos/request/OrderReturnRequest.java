package com.example.DATN.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReturnRequest {
    private UUID orderId;
    private String reason;
    private List<ReturnItemRequest> returnItems;
}
