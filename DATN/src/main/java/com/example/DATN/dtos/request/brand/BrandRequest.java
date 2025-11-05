package com.example.DATN.dtos.request.brand;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRequest {
    @NotEmpty(message = "BRAND_NAME_REQUIRED")
    private String name;
}