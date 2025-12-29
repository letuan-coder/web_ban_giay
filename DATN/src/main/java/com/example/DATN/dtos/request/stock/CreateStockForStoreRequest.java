package com.example.DATN.dtos.request.stock;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStockForStoreRequest {
    private Integer minQuantity;
}
