package com.example.DATN.dtos.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class ProductVariantRequest {

    private UUID id;
    @NotNull(message = "SIZE_REQUIRED")
    private SizeRequest size;

    private String sku;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String colorName;

    @Valid
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ColorRequest colors;

    @NotNull(message = "PRICE_REQUIRED")
    @Min(value = 0, message = "PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    @NotNull(message = "STOCK_REQUIRED")
    @Min(value = 1, message = "STOCK_MUST_BE_POSITIVE")
    private Integer stock;

}
