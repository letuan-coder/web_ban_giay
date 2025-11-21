package com.example.DATN.controllers;

import com.example.DATN.dtos.request.product.ProductRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.PageResponse;
import com.example.DATN.dtos.respone.product.ProductResponse;
import com.example.DATN.services.ImageProductService;
import com.example.DATN.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final ImageProductService imageProductService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductResponse> createProduct(
            @ModelAttribute @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .data(productService.createProduct(request))
                .build();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> uploadProducts(@RequestParam("file") MultipartFile file) {
        productService.addProductsFromExcel(file);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getAllProducts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProductResponse> productPage = productService.getAllProducts(pageable);
        PageResponse<ProductResponse> pageResponse = PageResponse.<ProductResponse>builder()
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .content(productPage.getContent())
                .build();
        return ApiResponse.<PageResponse<ProductResponse>>builder()
                .data(pageResponse)
                .build();
    }

    @GetMapping("/code/{productCode}")
    public ApiResponse<List<ProductResponse>> getProductByProductCode(
            @PathVariable String productCode) {
        return ApiResponse.<List<ProductResponse>>builder()
                .data(productService.getProductByProductCode(productCode))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductResponse>> searchProducts
            (@RequestParam("name") String name) {
        return ApiResponse.<List<ProductResponse>>builder()
                .data(productService.searchProductsByName(name))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable UUID id) {
        return ApiResponse.<ProductResponse>builder()
                .data(productService.getProductById(id))
                .build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable UUID id, @RequestBody @Valid ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .data(productService.updateProduct(id, request))
                .build();

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ApiResponse.<Void>builder().build();
    }

}