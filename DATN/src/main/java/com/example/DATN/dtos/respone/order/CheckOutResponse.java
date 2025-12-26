package com.example.DATN.dtos.respone.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutResponse {


    private List<CheckOutProductResponse> products;
    private UUID userAddressId;
    private Integer ExpectedFee;
}
