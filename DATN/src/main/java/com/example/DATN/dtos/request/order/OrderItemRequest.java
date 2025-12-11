package com.example.DATN.dtos.request.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private String sku;
    private Integer quantity;
//    private Integer weight;
//    private Integer height;
//    private Integer width;
//    private Integer length;
}