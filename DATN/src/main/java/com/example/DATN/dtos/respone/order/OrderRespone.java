package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRespone {
    private Long id;
    private List<OrderItemRespone> items;
    private Long paymentMethodId;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
}
