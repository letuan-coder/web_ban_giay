package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WareHouseResponse {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
}
