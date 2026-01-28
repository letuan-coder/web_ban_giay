package com.example.DATN.dtos.respone.cart;

import com.example.DATN.constant.Is_Available;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCartItemResponse {
    private UUID id;
    private String sku;
    private String name;
    private String size;
    private String color;
    private String thumbnailUrl;
    private Is_Available isAvailable;
    private BigDecimal price;
    @Builder.Default
    private BigDecimal discountPrice = BigDecimal.ZERO;

}
