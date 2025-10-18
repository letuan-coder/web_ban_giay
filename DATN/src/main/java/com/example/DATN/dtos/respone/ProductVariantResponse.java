package com.example.DATN.dtos.respone;

import com.example.DATN.Validator.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantResponse {
    private UUID id;
    private SizeResponse size;
    private ColorResponse color;
    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal price;
    private Integer stock;
    private String sku;
    private List<ImageProductResponse> images;
}
