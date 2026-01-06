package com.example.DATN.dtos.respone.order;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private String orderCode;
    private String userName;
    private ShippingAddressResponse shippingAddressResponse;
    private String paymentMethodName;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private BigDecimal totalPrice;
    private String response;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDateTime created_At;
    private List<OrderItemResponse> items;

}
