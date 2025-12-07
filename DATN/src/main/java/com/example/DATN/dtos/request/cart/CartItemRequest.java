package com.example.DATN.dtos.request.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemRequest {
//    UUID productVariantId;
    String sku;
    Integer quantity;
}
