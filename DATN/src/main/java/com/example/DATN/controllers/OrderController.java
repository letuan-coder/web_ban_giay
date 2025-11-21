package com.example.DATN.controllers;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.order.OrderItemRespone;
import com.example.DATN.dtos.respone.order.OrderRespone;
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
    public ApiResponse<OrderRespone> createOrder(@RequestBody @Valid OrderRequest request) {
        OrderRespone respone = orderService.createOrder(request);
        return ApiResponse.<OrderRespone>builder()
                .data(respone)
                .build();
    }


    @GetMapping
    public ApiResponse<List<OrderRespone>> getOrdersByUser() {
        List<OrderRespone> respone = orderService.getOrdersByUser();
        for (OrderRespone orderRespone : respone) {
            for (OrderItemRespone itemRespone : orderRespone.getItems()) {
                System.out.println(itemRespone.getSizeName());
            }
        }
        return ApiResponse.<List<OrderRespone>>builder()
                .data(respone)
                .build();
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderRespone> getOrderById(@PathVariable Long orderId) {
        return ApiResponse.<OrderRespone>builder()
                .data(orderService.getOrderById(orderId))
                .build();
    }
}
