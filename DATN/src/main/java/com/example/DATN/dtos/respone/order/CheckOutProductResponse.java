package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.Is_Available;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutProductResponse {
    private UUID id;
    private Is_Available isAvailable;
    private Integer quantity;
    private String productName;
    private String sku;
    private String colorName;
    private Integer sizeName;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
}
