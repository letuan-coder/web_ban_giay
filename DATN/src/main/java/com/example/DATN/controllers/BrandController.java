package com.example.DATN.controllers;

import com.example.DATN.dtos.request.brand.BrandRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.brand.BrandResponse;
import com.example.DATN.services.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
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
    public ApiResponse<BrandResponse> updateBrand(
            @PathVariable Long id, @RequestBody @Valid BrandRequest request) {
        return ApiResponse.<BrandResponse>builder()
                .data(brandService.updateBrand(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ApiResponse.<Void>builder().build();
    }
}