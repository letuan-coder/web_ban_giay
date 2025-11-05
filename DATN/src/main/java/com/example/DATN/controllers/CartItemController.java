package com.example.DATN.controllers;

import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.request.cart.UpdateCartIItemRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.mapper.CartItemMapper;
import com.example.DATN.services.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartItemMapper cartItemMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getAllCartItems() {
       ApiResponse response = ApiResponse.<List<CartItemResponse>>builder()
               .data(cartItemService.getAllCartItems())
               .build();
       return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItemResponse>> getCartItemById(
            @PathVariable UUID id) {
        ApiResponse response = ApiResponse.<CartItemResponse>builder()
                .data(cartItemService.getCartItemById(id))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ApiResponse<CartItemResponse> AddCartItem(
            @RequestBody CartItemRequest request) {
        CartItemResponse cartItem = cartItemService.AddCartItem(request);
        return ApiResponse.<CartItemResponse>builder()
                .data(cartItem)
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<CartItemResponse> updateCartItem(
            @RequestBody UpdateCartIItemRequest request) {
        CartItemResponse cartItem = cartItemService.updateCartItem( request);
        return ApiResponse.<CartItemResponse>builder()
                .data(cartItem)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable UUID id) {
        cartItemService.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }
}
