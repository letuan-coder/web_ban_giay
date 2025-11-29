package com.example.DATN.dtos.respone.product;


import com.example.DATN.constant.Is_Available;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class ProductColorResponse {
    UUID id;
    String colorCode;
    String colorName;
    String colorHexCode;
    Is_Available isAvailable;
    List<ImageProductResponse> images;
    List<ProductVariantResponse> variantResponses;
}
