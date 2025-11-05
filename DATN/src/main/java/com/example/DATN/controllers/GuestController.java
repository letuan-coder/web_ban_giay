package com.example.DATN.controllers;

import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.cart.GuestCartResponse;
import com.example.DATN.services.AuthenticationService;
import com.example.DATN.services.CartGuestService;
import com.example.DATN.services.CartItemGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
public class GuestController {
    private final CartGuestService cartGuestService;
    private final CartItemGuestService cartItemGuestService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ApiResponse<GuestCartResponse> GetCartGuest() {
        return ApiResponse.<GuestCartResponse>builder()
                .data(cartGuestService.getCart())
                .build();
    }

    @PostMapping("/cart")
    public ApiResponse<GuestCartResponse> AddItemToCart(
            @Valid @RequestBody CartItemRequest request){
        return ApiResponse.<GuestCartResponse>builder()
                .data(cartItemGuestService.AddGuestItemToCart(request))
                .build();
    }
    @PostMapping
    public ApiResponse<GuestCartResponse> createCartGuest() {
        return ApiResponse.<GuestCartResponse>builder()
                .data(cartGuestService.createCartForGuest())
                .build();
    }


}
