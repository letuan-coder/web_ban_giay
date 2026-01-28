package com.example.DATN.dtos.request.product;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VariantGroupRequest {
    private List<String> sizeCodes;
    private BigDecimal price;
    private BigDecimal importPrice;
    private int stock;
}
