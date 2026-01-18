package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GhnCreateOrderResponse {
    private int code;
    private String message;
    private Data data;

    @lombok.Data
    public static class Data {
        @JsonProperty("order_code")
        private String orderCode;

        @JsonProperty("expected_delivery_time")
        private LocalDateTime expectedDeliveryTime;
    }
}