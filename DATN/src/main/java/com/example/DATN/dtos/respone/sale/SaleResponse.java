package com.example.DATN.dtos.respone.sale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {
    private BigDecimal Total_Amount;
    private Map<String, BigDecimal> daily;
    private Map<String, BigDecimal> monthly;
    private Map<String, BigDecimal> yearly;
}
