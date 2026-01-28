package com.example.DATN.dtos.request;

import com.example.DATN.constant.StockType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor

@Getter
@Setter
public class AddStockRequest {
    private Long variantId;
    private StockType stockType;
    private Long locationId;
    private Integer quantity;
}
