package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRespone {
    private Long id;
    private List<OrderItemRespone> items;
    private String paymentMethodName;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    @JsonProperty("ngày đặt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime created_At;
}
