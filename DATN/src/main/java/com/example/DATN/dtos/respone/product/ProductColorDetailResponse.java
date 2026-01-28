package com.example.DATN.dtos.respone.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductColorDetailResponse {
    UUID productColorId;
    String colorName;
    String hexCode;
}
