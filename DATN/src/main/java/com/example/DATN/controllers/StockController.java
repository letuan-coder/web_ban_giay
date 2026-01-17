package com.example.DATN.controllers;

import com.example.DATN.dtos.request.StockRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.dtos.respone.StockTransactionResponse;
import com.example.DATN.services.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping
    public ApiResponse<StockTransactionResponse> createStock(
            @RequestBody StockRequest request) {
        StockTransactionResponse response= stockService.createStock(request);
        return ApiResponse.<StockTransactionResponse>builder()
                .data(response)
                .build();
    }



    @GetMapping
    public ApiResponse<List<StockResponse>> getAllStocks() {
        return ApiResponse.<List<StockResponse>>builder()
                .data(stockService.getAllStocks())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StockResponse> getStockById(@PathVariable UUID id) {
        return ApiResponse.<StockResponse>builder()
                .data(stockService.getStockById(id))
                .build();
    }



//    @PatchMapping("/{id}")
//    public ApiResponse<StockResponse> updateStock
//            (@PathVariable Long id, @RequestBody StockRequest request) {
//        return ApiResponse.<StockResponse>builder()
//                .data(stockService.updateStock(id, request))
//                .build();
//    }
//
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteStock(@PathVariable UUID id) {
        stockService.deleteStock(id);
        return ApiResponse.<String>builder()
                .data("Stock deleted successfully")
                .build();
    }
}