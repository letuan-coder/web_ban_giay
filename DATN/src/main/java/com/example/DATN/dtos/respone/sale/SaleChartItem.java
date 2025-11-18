package com.example.DATN.dtos.respone.sale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaleChartItem {
    private String time;
    private BigDecimal revenue;
}