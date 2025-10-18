package com.example.DATN.controllers;

import com.example.DATN.dtos.request.BrandRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.BrandResponse;
import com.example.DATN.services.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> createBrand(@RequestBody @Valid BrandRequest request) {
        return ApiResponse.<BrandResponse>builder()
                .data(brandService.createBrand(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BrandResponse>> getAllBrands() {
        return ApiResponse.<List<BrandResponse>>builder()
                .data(brandService.getAllBrands())
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<BrandResponse>> searchBrands
            (@RequestParam("name") String name) {
        return ApiResponse.<List<BrandResponse>>builder()
                .data(brandService.searchBrandsByName(name))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandResponse> getBrandById(@PathVariable Long id) {
        return ApiResponse.<BrandResponse>builder()
                .data(brandService.getBrandById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> updateBrand(@PathVariable Long id, @RequestBody @Valid BrandRequest request) {
        return ApiResponse.<BrandResponse>builder()
                .data(brandService.updateBrand(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.<Void>builder().build();
    }
}