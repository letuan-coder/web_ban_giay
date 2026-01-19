package com.example.DATN.dtos.respone.ghn;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GhnOrderSyncResponse {
    private int code;
    private String message;
    private GhnOrderDetailData data;
}
