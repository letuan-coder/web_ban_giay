package com.example.DATN.controllers;

import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.georaphy.DistrictResponse;
import com.example.DATN.services.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ApiResponse<List<DistrictResponse>> getAllDistricts() {
        List<DistrictResponse> districts = districtService.getAllDistricts();
        return ApiResponse.<List<DistrictResponse>>builder()
                .code(1000)
                .message("Success")
                .data(districts)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<List<DistrictResponse>> getDistrictById
            (@PathVariable String id) {
        List<DistrictResponse> districts = districtService.getDistrictsByProvinceCode(id);
        return ApiResponse.<List<DistrictResponse>>builder()
                .data(districts)
                .build();

    }
}
