package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WareHouseResponse {
    private UUID id;
    private String warehouseCode;
    private Integer provinceCode;
    private Integer districtCode;
    private Integer wardCode;
    private String name;
    private String location;
    private Integer capacity;
}
