package com.example.DATN.dtos.respone.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSaleResponse {
    private UUID productId;
    private String productName;
    private String productCode;
    private String thumbnailUrl;
    private BigDecimal price;
    private Long totalSold;
    private Long totalStock;
    private BigDecimal totalRevenue;
    private LocalDateTime createdAt;
}
