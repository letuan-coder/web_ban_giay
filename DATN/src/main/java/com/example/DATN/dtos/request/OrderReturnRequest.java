package com.example.DATN.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReturnRequest {
    private Long orderId;
    private String reason;
    private List<ReturnItemRequest> returnItems;
}
