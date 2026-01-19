package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnCreateOrderResponse {

    private int code;
    private String message;
    private GhnCreateOrderData data;
}