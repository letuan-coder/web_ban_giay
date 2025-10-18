package com.example.DATN.dtos.request;

import com.example.DATN.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Integer userId;
    private Integer paymentMethodId;
    private OrderStatus orderStatus;
    private List<OrderItemRequest> items;
}
