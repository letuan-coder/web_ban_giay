package com.example.DATN.controllers;

import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.request.product.UpdateProductColorRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.product.ImageProductResponse;
import com.example.DATN.dtos.respone.product.ProductColorResponse;
import com.example.DATN.mapper.ImageProductMapper;
import com.example.DATN.models.ImageProduct;
import com.example.DATN.services.ImageProductService;
import com.example.DATN.services.ProductColorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product-color")
public class ProductColorController {

    private final ProductColorService productColorService;
    private final ImageProductService imageProductService;
    private final ImageProductMapper imageProductMapper;

    @PostMapping(value = "/{productId}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ApiResponse<ProductColorResponse> createProductColor(
            @PathVariable UUID productId,
            @ModelAttribute ProductColorRequest request) {
        request.setProductId(productId);
        return ApiResponse.<ProductColorResponse>builder()
                .data(productColorService.createProductColor(request))
                .build();
    }

    @PostMapping(value = "/upload/{productColorId}", consumes = {"multipart/form-data"})
    public ApiResponse<List<ImageProductResponse>> UploadImage(
            @PathVariable UUID productColorId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "altTexts", required = false) List<String> altTexts) {
        List<ImageProduct> savedImages = imageProductService.uploadImages(productColorId, files, altTexts);
        List<ImageProductResponse> responses = imageProductMapper.toImageProductResponses(savedImages);
        return ApiResponse.<List<ImageProductResponse>>builder()
                .data(responses)
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
    public ApiResponse<ProductColorResponse> updateProductColor(
            @PathVariable UUID id,
            @RequestBody UpdateProductColorRequest request) {
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