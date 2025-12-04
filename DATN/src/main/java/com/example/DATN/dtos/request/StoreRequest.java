package com.example.DATN.dtos.request;

import lombok.Data;

@Data
public class StoreRequest {
    private String name;
    private Integer provinceCode;
    private Integer districtCode;
    private Integer wardCode;
    private String location;
    private String phoneNumber;
    private Boolean active;
}
