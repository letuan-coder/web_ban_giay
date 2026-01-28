package com.example.DATN.dtos.request;

import com.example.DATN.constant.TransactionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnWareHouseTransactionRequest {
    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    private String FromStore;
    private String ToWareHouse;
    private LocalDate expectedReceivedDate;


    @NotNull(message = "Items list cannot be null")
    @Size(min = 1, message = "Transaction must have at least one item")
    @Valid // This annotation triggers validation for each item in the list
    private List<StockTransactionItemRequest> items;

}
