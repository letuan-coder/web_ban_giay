package com.example.DATN.dtos.respone.product;


import com.example.DATN.constant.Is_Available;
import com.example.DATN.dtos.respone.ColorResponse;
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
    ColorResponse color;
    Is_Available isAvailable;
    List<ProductVariantResponse> variantResponses;
    List<ImageProductResponse> images;

}
