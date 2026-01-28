package com.example.DATN.dtos.respone.ghn;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShippingFeeResponse {
    private long total;
    private long serviceFee;
    private long insuranceFee;
    private long pickStationFee;
    private long couponValue;
    private long r2sFee;
    private long documentReturn;
    private long doubleCheck;
    private long codFee;
    private long pickRemoteAreasFee;
    private long deliverRemoteAreasFee;
    private long codFailedFee;

}
