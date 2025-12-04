package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private String orderCode;
    private String userName;
    private String userAddress;
    private String phoneNumber;
    private String receiverName;
    private String paymentMethodName;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime created_At;
    private List<OrderItemResponse> items;

}
