package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnCreateOrderData {
    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("expected_delivery_time")
    private OffsetDateTime expectedDeliveryTime;
}
