package com.example.DATN.controllers;

import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.request.product.UpdateProductColorRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.product.ProductColorResponse;
import com.example.DATN.services.ProductColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-colors")
@RequiredArgsConstructor
public class ProductColorController {

    private final ProductColorService productColorService;

    @PostMapping(value = "/{productId}" , consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductColorResponse> createProductColor
            (@PathVariable UUID productId,
             @ModelAttribute @Valid ProductColorRequest request) {
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductColorResponse> updateProductColor(@PathVariable UUID id, @RequestBody @Valid UpdateProductColorRequest request) {
        return ApiResponse.<ProductColorResponse>builder()
                .data(productColorService.updateProductColor(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProductColor(@PathVariable UUID id) {
        productColorService.deleteProductColor(id);
        return ApiResponse.<Void>builder().build();
    }
}
