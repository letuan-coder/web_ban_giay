package com.example.DATN.dtos.respone;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    UUID id;
    UUID cartId;
    ProductVariantResponse productVariant;
    Integer quantity;
}
