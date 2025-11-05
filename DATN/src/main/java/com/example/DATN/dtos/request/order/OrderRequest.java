package com.example.DATN.dtos.request.order;

import com.example.DATN.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private List<OrderItemRequest> items;
    private Long paymentMethodId;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
}