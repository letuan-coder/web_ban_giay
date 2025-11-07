package com.example.DATN.dtos.request.vnpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VnPaymentRequest {
    private Integer amount;
    private String bankCode;
    private String language;
}
