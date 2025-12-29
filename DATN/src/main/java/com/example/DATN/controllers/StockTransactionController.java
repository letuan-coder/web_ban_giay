package com.example.DATN.controllers;

import com.example.DATN.dtos.request.StockTransactionRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.StockTransactionResponse;
import com.example.DATN.mapper.StockTransactionMapper;
import com.example.DATN.services.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock-transactions")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService transactionService;
    private final StockTransactionMapper transactionMapper;

    @PostMapping
    public ApiResponse<String> createTransaction(
            @RequestBody StockTransactionRequest request) {
        transactionService.createTransaction(request);
        return ApiResponse.<String>builder()
                .data("Transaction created successfully.")
                .build();
    }


//    @PostMapping("/create-missing-items-invoice")
//    public ApiResponse<String> createMissingItemsInvoice(
//            @RequestBody CreateMissingItemsInvoiceRequest request) {
//        transactionService.createMissingItemsInvoice(request);
//        return ApiResponse.<String>builder()
//                .data("Missing items invoice created successfully.")
//                .build();
//    }

    @GetMapping
    public ApiResponse<List<StockTransactionResponse>> getAllTransactions() {
        List<StockTransactionResponse> responses = transactionService.getAllTransactions().stream()
                .map(transactionMapper::toStockTransactionResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<StockTransactionResponse>>builder()
                .data(responses)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StockTransactionResponse> getTransactionById(
            @PathVariable UUID id) {
        StockTransactionResponse response = transactionMapper.toStockTransactionResponse(
                transactionService.getTransactionById(id)
        );
        return ApiResponse.<StockTransactionResponse>builder()
                .data(response)
                .build();
    }
    @GetMapping("/code/{code}")
    public ApiResponse<StockTransactionResponse> getTransactionByCode(
            @PathVariable String code) {
        StockTransactionResponse response = transactionMapper.toStockTransactionResponse(
                transactionService.getTransactionByCode(code)
        );
        return ApiResponse.<StockTransactionResponse>builder()
                .data(response)
                .build();
    }
}
