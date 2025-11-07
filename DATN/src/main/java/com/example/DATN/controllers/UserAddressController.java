package com.example.DATN.controllers;

import com.example.DATN.dtos.request.user_address.UserAddressRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.user_address.UserAddressResponse;
import com.example.DATN.services.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @PostMapping
    public ApiResponse<UserAddressResponse> createAddress(
            @RequestBody UserAddressRequest request) {
        UserAddressResponse response = userAddressService.createUserAddress(request);
        return ApiResponse.<UserAddressResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<UserAddressResponse>> getAddresses() {
        return ApiResponse.<List<UserAddressResponse>>builder()
                .data(userAddressService.getUserAddresses())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAddressResponse> updateAddress(@PathVariable UUID id, @RequestBody @Valid UserAddressRequest request) {
        return ApiResponse.<UserAddressResponse>builder()
                .data(userAddressService.updateUserAddress(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable UUID id) {
        userAddressService.deleteUserAddress(id);
        return ApiResponse.<Void>builder().message("Address deleted successfully").build();
    }

    @PutMapping("/{id}/set-default")
    public ApiResponse<Void> setDefault(@PathVariable UUID id) {
        userAddressService.setDefaultAddress(id);
        return ApiResponse.<Void>builder().message("Address set as default successfully").build();
    }
}
