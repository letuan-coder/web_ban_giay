package com.example.DATN.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotEmpty(message = "PRODUCT_NAME_REQUIRED")
    private String name;
    private String description;
    private String productCode;

    @NotNull(message = "PRODUCT_PRICE_REQUIRED")
    @Min(value = 0, message = "PRODUCT_PRICE_MUST_BE_POSITIVE")
    private BigDecimal discountPrice;

    @NotNull(message = "PRODUCT_PRICE_REQUIRED")
    @Min(value = 0, message = "PRODUCT_PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    @Min(value = 0, message = "PRODUCT_STOCK_MUST_BE_NON_NEGATIVE")
    private Integer stock;

    @NotNull(message = "BRAND_ID_REQUIRED")
    private Long brandId;

    @NotNull(message = "CATEGORY_ID_REQUIRED")
    private Long categoryId;

    private List<ProductVariantRequest> variants;
}