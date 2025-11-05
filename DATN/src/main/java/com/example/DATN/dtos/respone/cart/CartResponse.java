package com.example.DATN.dtos.respone.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    UUID id;
    Long userId;
    BigDecimal total_price;
    List<CartItemResponse> cartItems;
}
