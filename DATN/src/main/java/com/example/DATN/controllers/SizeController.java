package com.example.DATN.controllers;

import com.example.DATN.dtos.request.SizeRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.services.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
public class SizeController {

    private final SizeService sizeService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<SizeResponse>>> createSize(@RequestBody List<SizeRequest> requests) {
        List<SizeResponse> responses = sizeService.createSize(requests);
        ApiResponse response= ApiResponse.<List<SizeResponse>>builder()
                .data(responses)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ApiResponse<List<SizeResponse>> getSizes() {
        List<SizeResponse> sizes =new ArrayList<>(sizeService.getSizes());
        sizes.sort((s1, s2) -> Integer.compare(s1.getName(), s2.getName())); // Sort by name (Integer) ascending
        return ApiResponse.<List<SizeResponse>>builder()
                .data(sizes)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<SizeResponse> getSizeById(@PathVariable String id) {
        return ApiResponse.<SizeResponse>builder()
                .data(sizeService.getSizeById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<SizeResponse> updateSize(@PathVariable String id, @RequestBody SizeRequest request) {
        return ApiResponse.<SizeResponse>builder()
                .data(sizeService.updateSize(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSize(@PathVariable String id) {
        sizeService.deleteSize(id);
        return ApiResponse.<String>builder()
                .data("Size deleted successfully")
                .build();
    }
}
