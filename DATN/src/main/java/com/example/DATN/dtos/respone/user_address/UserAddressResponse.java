package com.example.DATN.dtos.respone.user_address;

import lombok.Data;

import java.util.UUID;

@Data
public class UserAddressResponse {
    private UUID id;
    private String receiverName;
    private String phoneNumber;
    private String provinceName;
    private String provinceCode;
    private Integer districtCode;
    private String wardCode;
    private String districtName;
    private String wardName;
    private String streetDetail;
    private String userAddress;
    private boolean isDefault;
}
