package com.example.DATN.dtos.respone.cart;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class GuestCartResponse {
    UUID id;
    String guest_key;
    BigDecimal total_price;
    List<CartItemResponse> cartItems;

}
