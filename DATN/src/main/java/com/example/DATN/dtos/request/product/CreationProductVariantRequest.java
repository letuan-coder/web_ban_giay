package com.example.DATN.dtos.request.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreationProductVariantRequest {
    private UUID productColorId;
    private ProductVariantRequest variantRequest;
}