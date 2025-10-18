
package com.example.DATN.dtos.respone;

import com.example.DATN.Validator.PriceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal price;
    @JsonSerialize(using = PriceSerializer.class)
    private BigDecimal discountPrice;
    private int stock;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private List<ProductVariantResponse> variants;

}
