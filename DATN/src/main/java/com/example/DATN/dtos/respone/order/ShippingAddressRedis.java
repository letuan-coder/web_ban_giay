package com.example.DATN.dtos.respone.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingAddressRedis {
    private String receiverName;
    private String phoneNumber;
    private String provinceName;
    private String districtName;
    private String wardName;
    private String streetDetail;
    private String fullDetail;
}
