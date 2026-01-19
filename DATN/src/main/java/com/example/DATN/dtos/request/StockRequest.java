package com.example.DATN.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StockRequest {
    private String TransactionCode;
    private List<StockTransactionItemReceivedRequest> stockTransactionItemId;

}
