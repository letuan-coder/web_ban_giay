package com.example.DATN.dtos.respone.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private UUID id;
    private String productName;
    private String sku;
    private Boolean Rated;
    private String colorName;

    private String sizeName;
    private Integer quantity;
    private BigDecimal price;
    private String imageUrl;
}
