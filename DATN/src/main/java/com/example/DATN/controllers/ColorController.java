package com.example.DATN.controllers;

import com.example.DATN.dtos.request.ColorRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.ColorResponse;
import com.example.DATN.services.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    @PostMapping
    public ApiResponse<List<ColorResponse>> createColor(
            @RequestBody @Valid List<ColorRequest> requests) {
        return ApiResponse.<List<ColorResponse>>builder()
                .data(colorService.addColorList(requests))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ColorResponse>> getColors() {
        return ApiResponse.<List<ColorResponse>>builder()
                .data(colorService.getColors())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ColorResponse> getColorById(@PathVariable String id) {
        return ApiResponse.<ColorResponse>builder()
                .data(colorService.getColorById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ColorResponse> updateColor(
            @PathVariable String id
            , @RequestBody ColorRequest request) {
        return ApiResponse.<ColorResponse>builder()
                .data(colorService.updateColor(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteColor(@PathVariable String id) {
        colorService.deleteColor(id);
        return ApiResponse.<String>builder()
                .data("Color deleted successfully")
                .build();
    }
}
