package com.example.DATN.dtos.request.order;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.constant.ShippingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String Note;
    private Integer serviceId;
    private UUID userAddressesId;
    private Integer total_weight;
    private Integer total_height;
    private Integer total_width;
    private Integer total_length;
    private ShippingStatus ghnStatus;
    private OrderStatus orderStatus;
    private BigDecimal shippingFee;
    private LocalDateTime receivedDate;
    private List<OrderItemRequest> orderItemRequests;
    private PaymentMethodEnum type;
    private String bankCode;
}