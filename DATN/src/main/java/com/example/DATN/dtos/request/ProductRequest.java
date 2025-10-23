package com.example.DATN.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotEmpty(message = "PRODUCT_NAME_REQUIRED")
    private String name;
    private String description;
    private String productCode;

    @NotNull(message = "BRAND_ID_REQUIRED")
    private Long brandId;

    @NotNull(message = "CATEGORY_ID_REQUIRED")
    private Long categoryId;
}