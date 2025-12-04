package com.example.DATN.dtos.respone;

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
public class ReturnItemResponse {
    private UUID id;
    private UUID orderItemId;
    private String productName;
    private String productVariantName;
    private Integer quantity;
    private BigDecimal price;
}
