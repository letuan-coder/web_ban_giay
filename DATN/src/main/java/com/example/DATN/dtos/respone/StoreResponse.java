package com.example.DATN.dtos.respone;

import lombok.Data;

@Data
public class StoreResponse {
    private String code;
    private String name;
    private String location;
    private String phoneNumber;
    private Boolean active;
}
