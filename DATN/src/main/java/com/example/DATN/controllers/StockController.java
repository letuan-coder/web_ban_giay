package com.example.DATN.controllers;

import com.example.DATN.dtos.request.StockRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ApiResponse<StockResponse> createStock(
            @RequestBody StockRequest request) {
        return ApiResponse.<StockResponse>builder()
                .data(stockService.createStock(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<StockResponse>> getAllStocks() {
        return ApiResponse.<List<StockResponse>>builder()
                .data(stockService.getAllStocks())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StockResponse> getStockById(@PathVariable Long id) {
        return ApiResponse.<StockResponse>builder()
                .data(stockService.getStockById(id))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<StockResponse> updateStock
            (@PathVariable Long id, @RequestBody StockRequest request) {
        return ApiResponse.<StockResponse>builder()
                .data(stockService.updateStock(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteStock(@PathVariable Long id) {
        stockService.deleteStock(id);
        return ApiResponse.<String>builder()
                .data("Stock deleted successfully")
                .build();
    }
}