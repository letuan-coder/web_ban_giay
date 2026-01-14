package com.example.DATN.controllers;

import com.example.DATN.dtos.request.OrderReturnRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.OrderReturnResponse;
import com.example.DATN.services.OrderReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/order-returns")
@RequiredArgsConstructor
public class OrderReturnController {
    private final OrderReturnService orderReturnService;
    @PostMapping("/{orderId}")
        public ApiResponse<OrderReturnResponse> createOrderReturnRequest(
            @PathVariable UUID orderId,
            @RequestBody @Valid OrderReturnRequest request
            ) {

        request.setOrderId(orderId);
       OrderReturnResponse response=  orderReturnService.createReturnRequest(request);
         return ApiResponse.<OrderReturnResponse>builder()
                 .data(response)
                 .build();
    }
}
