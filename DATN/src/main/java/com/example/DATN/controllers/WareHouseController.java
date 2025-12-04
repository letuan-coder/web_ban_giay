package com.example.DATN.controllers;

import com.example.DATN.dtos.request.WareHouseRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.WareHouseResponse;
import com.example.DATN.services.WareHouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WareHouseController {

    private final WareHouseService wareHouseService;

    @PostMapping
    public ApiResponse<WareHouseResponse> createWareHouse
            (@RequestBody @Valid WareHouseRequest request) {
        return ApiResponse.<WareHouseResponse>builder()
                .data(wareHouseService.createWareHouse(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<WareHouseResponse>> getAllWareHouses() {
        return ApiResponse.<List<WareHouseResponse>>builder()
                .data(wareHouseService.getAllWareHouses())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<WareHouseResponse> getWareHouseById
            (@PathVariable String id) {
        return ApiResponse.<WareHouseResponse>builder()
                .data(wareHouseService.getWareHouseById(id))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<WareHouseResponse> updateWareHouse
            (@PathVariable String id, @RequestBody @Valid WareHouseRequest request) {
        return ApiResponse.<WareHouseResponse>builder()
                .data(wareHouseService.updateWareHouse(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWareHouse(
            @PathVariable String id
    ) {
        wareHouseService.deleteWareHouse(id);
        return ApiResponse.<Void>builder().build();
    }
}
