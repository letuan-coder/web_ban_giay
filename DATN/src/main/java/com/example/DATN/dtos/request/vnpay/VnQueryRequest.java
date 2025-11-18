package com.example.DATN.dtos.request.vnpay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VnQueryRequest {
    private String orderId;
    private String transDate;
}
