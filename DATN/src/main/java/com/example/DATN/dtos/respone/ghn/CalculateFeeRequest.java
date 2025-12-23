package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateFeeRequest {
    @JsonProperty("from_district_id")
    private int fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("service_id")
    private int serviceId;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId; // null được

    @JsonProperty("to_district_id")
    private int toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    private int height;
    private int length;
    private int weight;
    private int width;

    @JsonProperty("insurance_value")
    private int insuranceValue;

    @JsonProperty("cod_failed_amount")
    private int codFailedAmount;

    private String coupon; // null được

    private List<ItemRequest> items;
}
