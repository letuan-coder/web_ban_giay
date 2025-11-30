package com.example.DATN.dtos.respone.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private String productName;
    private String sku;
    private String colorName;
    private String sizeName;
    private Integer quantity;
    private BigDecimal discountPrice;
    private BigDecimal price;
    private String imageUrl;
}
