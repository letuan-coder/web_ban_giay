package com.example.DATN.controllers;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.order.OrderItemResponse;
import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody @Valid OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ApiResponse.<OrderResponse>builder()
                .data(response)
                .build();
    }


    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrdersByUser() {
        List<OrderResponse> response = orderService.getOrdersByUser();
        for (OrderResponse OrderResponse : response) {
            for (OrderItemResponse itemResponse : OrderResponse.getItems()) {
                System.out.println(itemResponse.getSizeName());
            }
        }
        return ApiResponse.<List<OrderResponse>>builder()
                .data(response)
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ApiResponse.<OrderResponse>builder()
                .data(orderService.getOrderById(orderId))
                .build();
    }
}
