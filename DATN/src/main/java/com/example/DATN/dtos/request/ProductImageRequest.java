package com.example.DATN.dtos.request;

import com.example.DATN.models.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequest {

    private ProductVariant productVariant;

    private String imageUrl;

    private String altText;
}
