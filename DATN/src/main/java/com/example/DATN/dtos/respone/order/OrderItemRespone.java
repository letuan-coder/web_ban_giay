package com.example.DATN.dtos.respone.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRespone {
    private Long id;
    private String productName;
    private String colorName;
    private String sizeName;
    private Integer quantity;
    private BigDecimal discountPrice;
    private BigDecimal price;
    private String image;
}
