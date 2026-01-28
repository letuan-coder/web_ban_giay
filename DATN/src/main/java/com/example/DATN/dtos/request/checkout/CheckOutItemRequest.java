package com.example.DATN.dtos.request.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutItemRequest {
    private String sku;
    private Integer quantity;
}
