package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculateFeeRequest {
    @JsonProperty("from_district_id")
    private int from_district_id;

    @JsonProperty("from_ward_code")
    private String from_ward_code;

    @JsonProperty("service_id")
    private int service_id;

    @JsonProperty("service_type_id")
    private Integer service_type_id;

    @JsonProperty("to_district_id")
    private int to_district_id;

    @JsonProperty("to_ward_code")
    private String to_ward_code;

    private int height;
    private int length;
    private int weight;
    private int width;

    @JsonProperty("insurance_value")
    private int insurance_value;

    @JsonProperty("cod_failed_amount")
    private int cod_failed_amount;

    private String coupon;

    private List<ItemRequest> items;
}
