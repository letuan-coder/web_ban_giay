package com.example.DATN.dtos.request.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressRequest {
    private String receiverName;
    private String phoneNumber;
    private String provinceName;
    private String districtName;
    private Integer district_Id;
    private String wardCode;
    private String wardName;
    private String streetDetail;
    private String fullDetail;
}
