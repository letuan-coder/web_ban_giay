package com.example.DATN.dtos.respone.user_address;

import lombok.Data;

import java.util.UUID;

@Data
public class UserAddressResponse {
    private UUID id;

    private String receiverName;

    private String phoneNumber;

    private String provinceName;

    private String districtName;

    private String communeName;

    private String streetDetail;

    private String userAddress;

    private boolean isDefault;
}
