package com.example.DATN.dtos.respone.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProductResponse {
    private String name;
    private List<ProductVariantDetailResponse> variantDetailResponses;

}
