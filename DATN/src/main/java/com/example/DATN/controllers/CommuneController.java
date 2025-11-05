package com.example.DATN.controllers;

import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.georaphy.CommuneResponse;
import com.example.DATN.services.CommuneService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/communes")
@RequiredArgsConstructor
public class CommuneController {

    private final CommuneService communeService;

    @GetMapping
    public ApiResponse<List<CommuneResponse>> getAllCommunes() {
        List<CommuneResponse> communes = communeService.getAllCommunes();
        return ApiResponse.<List<CommuneResponse>>builder()
                .code(1000)
                .message("Success")
                .data(communes)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<List<CommuneResponse>> getDistrictById
            (@PathVariable String id) {
        List<CommuneResponse> communes = communeService.getCommunesByDistrictCode(id);
        return ApiResponse.<List<CommuneResponse>>builder()
                .data(communes)
                .build();

    }
}
