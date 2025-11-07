package com.example.DATN.dtos.respone.user_address;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class UserAddressResponse {
    @JsonProperty("Mã địa chỉ")
    private UUID id;

    @JsonProperty("Người nhận")
    private String receiverName;

    @JsonProperty("Số điện thoại")
    private String phoneNumber;

    @JsonProperty("Tỉnh/Thành phố")
    private String provinceName;

    @JsonProperty("Quận/Huyện")
    private String districtName;

    @JsonProperty("Phường/Xã")
    private String communeName;

    @JsonProperty("Địa chỉ")
    private String streetDetail;

    @JsonProperty("Địa chỉ chi tiết")
    private String userAddress;

    @JsonProperty("Mặc định")
    private boolean isDefault;
}
