package com.example.DATN.dtos.respone.cart;

import com.example.DATN.dtos.respone.product.ProductVariantResponse;
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
