package com.example.DATN.dtos.respone.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutResponse {
    private List<CheckOutProductResponse> products;
    private double distance;
    private String messageForUser;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID userAddressId;
    private ShippingAddressRedis shippingAddressResponse;
    private UUID stockId;
    private UUID storeId;

    private Integer total_weight;
    private Integer total_length;
    private Integer total_width;
    private Integer total_height;

    private String voucherCode;
    private BigDecimal voucherDiscount;
    private String voucherName;
    private UUID voucherId;

    private BigDecimal shippingFee;
    private BigDecimal finalPrice;
}
