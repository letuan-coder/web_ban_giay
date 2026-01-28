package com.example.DATN.controllers;

import com.example.DATN.dtos.request.StoreRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.StoreResponse;
import com.example.DATN.services.StoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;


    @PostMapping
    public ApiResponse<StoreResponse> createStore(
            @RequestBody StoreRequest request) throws JsonProcessingException {
        return ApiResponse.<StoreResponse>builder()
                .data(storeService.createStore(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<StoreResponse>> getAllStores() {
        return ApiResponse.<List<StoreResponse>>builder()
                .data(storeService.getAllStores())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StoreResponse> getStoreById
            (@PathVariable UUID id) {
        return ApiResponse.<StoreResponse>builder()
                .data(storeService.getStoreById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<StoreResponse> updateStore
            (@PathVariable UUID id, @RequestBody StoreRequest request) {
        return ApiResponse.<StoreResponse>builder()
                .data(storeService.updateStore(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteStore(
            @PathVariable String id) {
        storeService.deleteStore(id);
        return ApiResponse.<String>builder()
                .data("Store deleted successfully")
                .build();
    }
}