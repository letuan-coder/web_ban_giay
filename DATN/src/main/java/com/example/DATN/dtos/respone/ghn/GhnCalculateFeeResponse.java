package com.example.DATN.dtos.respone.ghn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class GhnCalculateFeeResponse {
    private int code;
    private String message;
    private ShippingFeeResponse data;
}
