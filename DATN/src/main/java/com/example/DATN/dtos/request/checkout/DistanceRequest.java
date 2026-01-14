package com.example.DATN.dtos.request.checkout;


import com.example.DATN.dtos.respone.order.ShippingAddressRedis;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceRequest {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ShippingAddressRedis shippingAddressRedis;
    private UUID userAddressId;
    private String idempotencyKey;

    @AssertTrue(message = "Phải truyền shippingAddressRedis hoặc userAddressId")
    public boolean isOnlyOneAddressSource() {
        return (shippingAddressRedis == null) ^ (userAddressId == null);
    }
}
