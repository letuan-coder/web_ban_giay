package com.example.DATN.controllers;

import com.example.DATN.dtos.request.CreationProductVariantRequest;
import com.example.DATN.dtos.request.ProductVariantRequest;
import com.example.DATN.dtos.request.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.ProductVariantResponse;
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

    @PostMapping("/{product_color_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createProductVariant(
            @PathVariable UUID product_color_id,
            @Valid @ModelAttribute ProductVariantRequest variantRequest) {
        CreationProductVariantRequest request = CreationProductVariantRequest.builder()
                .productColorId(product_color_id)
                .variantRequest(variantRequest)
                .build();
        ApiResponse response = ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.createProductVariant(request))
                .build();
        return ResponseEntity.ok(response);
    }

//    @Transactional
//    @GetMapping("/product/{productId}")
//    public ApiResponse<List<ProductVariantResponse>> getProductVariantsByProductId(
//            @PathVariable UUID productId) {
//        return ApiResponse.<List<ProductVariantResponse>>builder()
//                .data(productVariantService.getProductVariantsByProductId(productId))
//                .build();
//    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVariantResponse> getProductVariantById(@PathVariable UUID id) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.getProductVariantById(id))
                .build();
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateProductVariant(
            @PathVariable UUID id ,
            @ModelAttribute @Valid UpdateProductVariantRequest updaterequest) {
        ApiResponse response = ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.updateProductVariant(id, updaterequest))
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
