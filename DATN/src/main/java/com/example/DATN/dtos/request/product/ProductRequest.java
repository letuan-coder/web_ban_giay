package com.example.DATN.dtos.request.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotEmpty(message = "PRODUCT_NAME_REQUIRED")
    private String name;
    private String description;
    private String productCode;
    private MultipartFile file;
    private String ThumbnailUrl;
    private String altText;
    private BigDecimal price;
    @NotNull(message = "BRAND_ID_REQUIRED")
    private Long brandId;
    private Double weight;
    @NotNull(message = "CATEGORY_ID_REQUIRED")
    private Long categoryId;
}