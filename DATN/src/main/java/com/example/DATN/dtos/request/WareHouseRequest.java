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
   private String addressDetail;
    private Integer provinceCode;
    private Integer districtCode;
    private Integer wardCode;
    private String location;
//    @NotNull (message = "CAPACITY_IS_NULL")
    private Integer capacity;
    private Boolean isCentral;
}
