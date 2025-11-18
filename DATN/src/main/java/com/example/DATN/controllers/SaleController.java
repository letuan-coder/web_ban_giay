package com.example.DATN.controllers;

import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.sale.SaleResponse;
import com.example.DATN.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sales")
public class SaleController {
    private final SaleService saleService;
    @GetMapping
    public ResponseEntity<ApiResponse<SaleResponse>> getSalePage() {
        SaleResponse saleResponse = saleService.getDataChart();
        ApiResponse<SaleResponse> response = ApiResponse.<SaleResponse>builder()
                .data(saleResponse)
                .build();
        return ResponseEntity.ok(response);
    }

}
