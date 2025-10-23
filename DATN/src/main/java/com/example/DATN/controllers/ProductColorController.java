package com.example.DATN.controllers;

import com.example.DATN.dtos.request.ProductColorRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.ProductColorResponse;
import com.example.DATN.services.ProductColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-color")
public class ProductColorController {

    private final ProductColorService productColorService;

    @PostMapping(value = "/{productId}", consumes = {"multipart/form-data"})
    public ApiResponse<ProductColorResponse> createProductColor(
            @PathVariable UUID productId,
            @ModelAttribute ProductColorRequest request) {
        request.setProductId(productId);
        return ApiResponse.<ProductColorResponse>builder()
                .data(productColorService.createProductColor(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ProductColorResponse>> getAllProductColors() {
        return ApiResponse.<List<ProductColorResponse>>builder()
                .data(productColorService.getAllProductColors())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductColorResponse> getProductColorById(@PathVariable UUID id) {
        return ApiResponse.<ProductColorResponse>builder()
                .data(productColorService.getProductColorById(id))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductColorResponse> updateProductColor(@PathVariable UUID id, @RequestBody ProductColorRequest request) {
        return ApiResponse.<ProductColorResponse>builder()
                .data(productColorService.updateProductColor(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProductColor(@PathVariable UUID id) {
        productColorService.deleteProductColor(id);
        return ApiResponse.<Void>builder().build();
    }
}