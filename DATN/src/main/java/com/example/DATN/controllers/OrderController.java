package com.example.DATN.controllers;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.order.OrderRespone;
import com.example.DATN.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderRespone> createOrder
            (@RequestBody OrderRequest orderRequest) {
        return ApiResponse.<OrderRespone>builder()
                .data(orderService.createOrder(orderRequest))
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderRespone>> getOrdersByUser() {
        return ApiResponse.<List<OrderRespone>>builder()
                .data(orderService.getOrdersByUser())
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderRespone> getOrderById(@PathVariable Long orderId) {
        return ApiResponse.<OrderRespone>builder()
                .data(orderService.getOrderById(orderId))
                .build();
    }
}
