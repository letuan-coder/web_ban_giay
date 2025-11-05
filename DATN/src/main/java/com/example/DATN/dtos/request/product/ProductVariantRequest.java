package com.example.DATN.dtos.request.product;

import com.example.DATN.dtos.request.SizeRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class ProductVariantRequest {
    private UUID id;

//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    private SizeRequest size;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SizeRequest> sizes;

    private String sku;

    @Min(value = 0, message = "PRICE_MUST_BE_POSITIVE")
    private BigDecimal price;

    @NotNull(message = "STOCK_REQUIRED")
    @Min(value = 1, message = "STOCK_MUST_BE_POSITIVE")
    private Integer stock;
}
