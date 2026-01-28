package com.example.DATN.dtos.request.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequest {
    List<CheckOutItemRequest> item;
    String voucherCode;
    String idempotencyKey;
}
