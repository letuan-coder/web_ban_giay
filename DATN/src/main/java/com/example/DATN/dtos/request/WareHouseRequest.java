package com.example.DATN.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WareHouseRequest {
    @NotBlank(message = "WAREHOUSE_NAME_NOT_BLANK")
    private String name;
    private String location;
    private Integer capacity;
}
