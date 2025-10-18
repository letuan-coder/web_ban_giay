package com.example.DATN.controllers;

import com.example.DATN.dtos.request.CreationProductVariantRequest;
import com.example.DATN.dtos.request.ProductRequest;
import com.example.DATN.dtos.request.ProductVariantRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.ProductResponse;
import com.example.DATN.dtos.respone.ProductVariantResponse;
import com.example.DATN.mapper.ProductVariantMapper;
import com.example.DATN.services.ColorService;
import com.example.DATN.services.ImageProductService;
import com.example.DATN.services.ProductService;
import com.example.DATN.services.ProductVariantService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional(rollbackOn = Exception.class)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createProductVariant(
            @Valid @ModelAttribute ProductRequest productRequest,
            @Valid @ModelAttribute ProductVariantRequest variantRequest,
            @RequestParam(required = false) List<MultipartFile> files,
            @RequestParam(required = false) List<String> altText) {
        CreationProductVariantRequest request= CreationProductVariantRequest.builder()
                .productRequest(productRequest)
                .variantRequest(variantRequest)
                .files(files)
                .altText(altText)
                .build();
        ApiResponse response = ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.createProductVariant(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @Transactional
    @GetMapping("/product/{productId}")
    public ApiResponse<List<ProductVariantResponse>> getProductVariantsByProductId(
            @PathVariable UUID productId) {
        return ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.getProductVariantsByProductId(productId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVariantResponse> getProductVariantById(@PathVariable UUID id) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.getProductVariantById(id))
                .build();
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateProductVariant(
            @PathVariable UUID id,
            @RequestBody @Valid ProductVariantRequest request,
            @RequestBody @Valid ProductRequest productRequest) {
        ProductResponse productResponse = productService.createProduct(productRequest);
        UUID prodcutID = productResponse.getId();
        ApiResponse response = ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.updateProductVariant(id, request, prodcutID))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProductVariant(@PathVariable UUID id) {
        productVariantService.deleteProductVariant(id);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/get-all-variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> GetAllVariants(){
        ApiResponse response= ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.getallproductvariant())
                .build();
        return ResponseEntity.ok(response);
    }
}
