package com.example.DATN.dtos.request.product;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotEmpty(message = "PRODUCT_NAME_REQUIRED")
    private String name;
    private String description;
    private MultipartFile file;
    private BigDecimal price;
    private BigDecimal importPrice;
    private List<String> colorCodes;
    private List<String> sizeCodes;
    private UUID supplierId;
    @NotNull(message = "BRAND_ID_REQUIRED")
    private Long brandId;
    private Double weight;
    @NotNull(message = "CATEGORY_ID_REQUIRED")
    private Long categoryId;

}