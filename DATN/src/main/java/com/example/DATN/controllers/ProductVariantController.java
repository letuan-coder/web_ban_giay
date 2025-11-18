package com.example.DATN.controllers;


import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.request.product.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.services.ColorService;
import com.example.DATN.services.ImageProductService;
import com.example.DATN.services.ProductService;
import com.example.DATN.services.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductService productService;
    private final ProductVariantService productVariantService;
    private final ImageProductService imageProductService;
    private final ColorService colorService;
    private final ProductVariantMapper productVariantMapper;

    @PostMapping(value = "/list/{product_color_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> createListProductVariant(
            @PathVariable UUID product_color_id,
            @Valid @RequestBody List<ProductVariantRequest> variantRequest) {
        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.createListProductVariant(product_color_id, variantRequest))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVariantResponse> getProductVariantById(
            @PathVariable UUID id) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.getProductVariantById(id))
                .build();
    }

    @PatchMapping("/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> updateProductVariantByColor(
            @PathVariable UUID id,
            @RequestBody @Valid List<UpdateProductVariantRequest> listofupdaterequest) {
        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.updateProductVariant(id, listofupdaterequest))
                .build();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProductVariant(@PathVariable UUID id) {
        productVariantService.deleteProductVariant(id);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProductColor(@PathVariable UUID id) {
        productVariantService.deleteProductColor(id);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> updateProductVariant(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductVariantRequest request) {
        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.UpdateProductVariantById(id, request))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> GetAllVariants() {
        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.getallproductvariant())
                .build();
        return ResponseEntity.ok(response);
    }
}
