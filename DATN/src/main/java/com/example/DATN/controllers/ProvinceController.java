package com.example.DATN.controllers;

import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.georaphy.ProvinceResponse;
import com.example.DATN.services.ProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ProvinceService provinceService;

    @GetMapping
    public ApiResponse<List<ProvinceResponse>> getAllProvinces() {
        List<ProvinceResponse> provinces = provinceService.getAllProvinces();
        return ApiResponse.<List<ProvinceResponse>>builder()
                .code(1000)
                .message("Success")
                .data(provinces)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProvinceResponse> getProvinceById(@PathVariable String id) {
        return provinceService.getProvinceById(id)
                .map(province -> ApiResponse.<ProvinceResponse>builder()
                        .code(1000)
                        .message("Success")
                        .data(province)
                        .build())
                .orElse(ApiResponse.<ProvinceResponse>builder()
                        .code(1004)
                        .message("Province not found")
                        .build());
    }
}
