package com.example.DATN.dtos.respone.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutResponse {
    private List<CheckOutProductResponse> products;
    private String from;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID userAddressId;
    private ShippingAddressRedis shippingAddressResponse;
    private UUID storeId;
    private String voucherCode;
    private BigDecimal voucherDiscount;
    private BigDecimal shippingFee;
    private BigDecimal weightFee;
    private BigDecimal distanceFee;
    private BigDecimal quantityFee;
    private BigDecimal finalPrice;
}
