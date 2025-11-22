package com.example.DATN.controllers;

import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.order.OrderItemResponse;
import com.example.DATN.services.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping("/order/{orderId}")
    public ApiResponse<OrderItemResponse> addOrderItemToOrder(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequest itemRequest) {
        return ApiResponse.<OrderItemResponse>builder()
                .data(orderItemService.addOrderItemToOrder(orderId, itemRequest))
                .build();
    }

    @PutMapping("/{itemId}")
    public ApiResponse<OrderItemResponse> updateOrderItemQuantity(@PathVariable Long itemId, @RequestParam int quantity) {
        return ApiResponse.<OrderItemResponse>builder()
                .data(orderItemService.updateOrderItemQuantity(itemId, quantity))
                .build();
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<Void> removeOrderItem(@PathVariable Long itemId) {
        orderItemService.removeOrderItem(itemId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/{itemId}")
    public ApiResponse<OrderItemResponse> getOrderItemById(@PathVariable Long itemId) {
        return ApiResponse.<OrderItemResponse>builder()
                .data(orderItemService.getOrderItemById(itemId))
                .build();
    }
}
