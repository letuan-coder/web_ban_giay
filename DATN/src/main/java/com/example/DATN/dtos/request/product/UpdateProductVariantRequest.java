package com.example.DATN.dtos.request.product;

import com.example.DATN.dtos.request.ColorRequest;
import com.example.DATN.dtos.request.SizeRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProductVariantRequest {
    private UUID id;
    private SizeRequest size;
    private String colorName;
    private ColorRequest colors;
    private BigDecimal price;
    private Integer stock;
    private String sku;
    private BigDecimal discountPrice;
}