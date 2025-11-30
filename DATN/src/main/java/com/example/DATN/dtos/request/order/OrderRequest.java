package com.example.DATN.dtos.request.order;

import com.example.DATN.constant.PaymentMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String Note;
    private Integer serviceId;
    private List<OrderItemRequest> orderItemRequests;
    private PaymentMethodEnum type;
}