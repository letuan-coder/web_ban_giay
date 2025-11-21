package com.example.DATN.controllers;

import com.example.DATN.constant.BannerType;
import com.example.DATN.dtos.request.BannerRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.BannerResponse;
import com.example.DATN.services.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @PostMapping(value = "/formdata", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<BannerResponse> createBanner(
            @RequestPart("data") @Valid BannerRequest request,
            @RequestPart("file") MultipartFile file) {
        request.setFile(file);
        return ApiResponse.<BannerResponse>builder()
                .data(bannerService.createBanner(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<BannerResponse>> getAllBanners() {
        return ApiResponse.<List<BannerResponse>>builder()
                .data(bannerService.getAllBanners())
                .build();
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<BannerResponse>> getBannersByType(@PathVariable("type") BannerType type) {
        return ApiResponse.<List<BannerResponse>>builder()
                .data(bannerService.getBannersByType(type))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<BannerResponse> updateBanner(
            @PathVariable UUID id, @RequestBody @Valid BannerRequest request) {
        return ApiResponse.<BannerResponse>builder()
                .data(bannerService.updateBanner(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteBanner(
            @PathVariable UUID id) {
        bannerService.deleteBanner(id);
        return ApiResponse.<String>builder()
                .data("Banner deleted successfully")
                .build();
    }
}
