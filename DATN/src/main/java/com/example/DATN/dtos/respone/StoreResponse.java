package com.example.DATN.dtos.respone;

import lombok.Data;

import java.util.UUID;

@Data
public class StoreResponse {
    private UUID id;
    private String code;
    private String name;
    private String location;
    private String phoneNumber;
    private Boolean active;
}
