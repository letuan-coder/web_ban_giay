package com.example.DATN.dtos.respone.product;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.SizeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
    private UUID id;
    private SizeResponse size;
    private Is_Available isAvailable;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stock;
    private String sku;
    private LocalDate createdAt;
}
