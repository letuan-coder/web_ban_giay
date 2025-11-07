package com.example.DATN.controllers;

import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.cart.CartResponse;
import com.example.DATN.mapper.CartMapper;
import com.example.DATN.models.Cart;
import com.example.DATN.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@EnableWebSecurity
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @GetMapping("/{id}")
    public ApiResponse<CartResponse> getCartById(
            @PathVariable UUID id) {
        return ApiResponse.<CartResponse>builder()
                .data(cartService.getCartById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<CartResponse> createCart() {
        CartResponse response=cartService.createCartForUser();

        return ApiResponse.<CartResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCartByUserId(){
        ApiResponse<CartResponse> response =ApiResponse.<CartResponse>builder()
                .data(cartService.getCartByUserId())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<CartResponse> updateCart(@PathVariable UUID id, @RequestBody Cart cart) {
        cart.setId(id);
        return ApiResponse.<CartResponse>builder()
                .data((cartMapper.toCartResponse(cartService.updateCart(cart))))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable UUID id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }
}
