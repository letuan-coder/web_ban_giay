package com.example.DATN.controllers;

import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.request.product.UpdateProductColorRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.product.ImageProductResponse;
import com.example.DATN.dtos.respone.product.ProductColorResponse;
import com.example.DATN.services.ProductColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-colors")
@RequiredArgsConstructor
public class ProductColorController {

    private final ProductColorService productColorService;

    @PostMapping(value = "/{productId}", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductColorResponse> createProductColor
            (@PathVariable UUID productId,
             @ModelAttribute @Valid ProductColorRequest request) {
        request.setProductId(productId);
        return ApiResponse.<ProductColorResponse>builder()
                .data(productColorService.createProductColor(request))
                .build();
    }

    @GetMapping(value = "/images/{productColorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<ImageProductResponse>> getImageColor
            (@PathVariable UUID productColorId) {
       List<ImageProductResponse> response = productColorService.getImageById(productColorId);
        return ApiResponse.<List<ImageProductResponse>>builder()
                .data(response)
                .build();
    }
    @PostMapping(value = "/upload/{productColorId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductColorResponse> UplpadProductColor
            (@PathVariable UUID productColorId,
             @RequestPart(name = "files") List<MultipartFile> files,
             @ModelAttribute @Valid ProductColorRequest request) {

        request.setFiles(files);
        request.setProductId(productColorId);
        productColorService.UploadColorImage(request);
        return ApiResponse.<ProductColorResponse>builder()
                .data(null)
                .message("Upload image success")
                .build();
    }
    @GetMapping
    public ApiResponse<List<ProductColorResponse>> getAllProductColors() {
        return ApiResponse.<List<ProductColorResponse>>builder()
                .data(productColorService.getAllProductColors())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductColorResponse> getProductColorById
            (@PathVariable UUID id) {
        ProductColorResponse response = productColorService.getProductColorById(id);
        return ApiResponse.<ProductColorResponse>builder()
                .data(response)
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductColorResponse> updateProductColor(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductColorRequest request) {
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
