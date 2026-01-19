package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromotionPriceResponse {
    private BigDecimal originalPrice;
    private BigDecimal discountPrice;
}
