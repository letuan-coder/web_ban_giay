package com.example.DATN.dtos.respone.product;

import com.example.DATN.constant.Is_Available;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDetailResponse {
    private UUID id;
    private Integer size;
    private Is_Available isAvailable;
    private BigDecimal price;
    private Integer stock;
}
