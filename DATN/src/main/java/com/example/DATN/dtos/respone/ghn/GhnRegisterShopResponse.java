package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class GhnRegisterShopResponse {
    private int code;
    private String message;
    private DataResponse data;

    @Data
    public static class DataResponse {
        @JsonProperty("shop_id")
        private Integer shopId;
    }
}
