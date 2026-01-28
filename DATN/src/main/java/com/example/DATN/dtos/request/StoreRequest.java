package com.example.DATN.dtos.request;

import lombok.Data;

@Data
public class StoreRequest {
    private String name;
    private Integer provinceCode;
    private Integer districtCode;
    private String wardCode;
    private String wardName;
    private String districtName;
    private String provinceName;
    private String location;
    private String phoneNumber;
    private Boolean active;
    private Double longitude;
    private Double latitude;
}
