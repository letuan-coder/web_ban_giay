package com.example.DATN.dtos.respone.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProductResponse {
    private UUID id;
    private String name;
    private List<ProductVariantDetailResponse> variantDetailResponses;

}
