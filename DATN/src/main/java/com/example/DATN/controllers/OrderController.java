package com.example.DATN.controllers;

import com.example.DATN.constant.PaymentMethodEnum;
import com.example.DATN.dtos.request.order.CancelOrderRequest;
import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.order.CancelOrderResponse;
import com.example.DATN.dtos.respone.order.OrderItemResponse;
import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repositories.UserAddressRepository;
import com.example.DATN.services.CheckOutService;
import com.example.DATN.services.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final CheckOutService checkOutService;
    private final UserAddressRepository userAddressRepository;

    @PostMapping("/admin/confirm-orders")
    public ApiResponse<OrderResponse> confirmOrder(
            @RequestBody @Valid UUID orderId) {
        orderService.confirmOrder(orderId);
        return ApiResponse.<OrderResponse>builder()
                .data(null)
                .message("confirm order !!!")
                .build();
    }


    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @RequestBody @Valid OrderRequest request
            , @RequestHeader(value = "X-Idempotency-Key", required = false) String headerKey
            , HttpServletRequest servletRequest) throws JsonProcessingException {
        String idempotencyKey = headerKey != null ? headerKey : request.getIdempotency();
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        request.setIdempotency(idempotencyKey);

        OrderResponse response = new OrderResponse();
        if (request.getType() == PaymentMethodEnum.CASH_ON_DELIVERY) {
            response = orderService.createOrder(request);
        } else {
            response = orderService.createOrderVnPay(request, servletRequest);
        }
        return ApiResponse.<OrderResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/cancel-order/{orderId}")
    public ApiResponse<CancelOrderResponse> cancelOrder(
            @PathVariable  UUID orderId,
            @RequestBody @Valid CancelOrderRequest request,
            @RequestHeader (value = "X-Idempotency-Key",required = false) String headerKey
            ,HttpServletRequest req) {
        String idempotencyKey = headerKey != null ? headerKey : request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        request.setIdempotencyKey(idempotencyKey);
        request.setOrderId(orderId);
      CancelOrderResponse response=  orderService.cancelOrder(request, req);
        return ApiResponse.<CancelOrderResponse>builder()
                .data(response)
                .build();
    }

    @PatchMapping("/{orderId}")
    public ApiResponse<OrderResponse> updateOrderId(
            @PathVariable UUID orderId,
            @RequestBody String newStatus
    ) {
        orderService.updateOrderStatus(orderId, newStatus);
        return ApiResponse.<OrderResponse>builder()
                .data(null)
                .message("update status for order")
                .build();
    }

    @PatchMapping("/{orderId}/shipping")
    public ApiResponse<OrderResponse> updateShippingStatus(
            @PathVariable UUID orderId,
            @RequestBody String newStatus
    ) {
        orderService.updateShippingStatus(orderId, newStatus);
        return ApiResponse.<OrderResponse>builder()
                .data(null)
                .message("update shipping status for order")
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<OrderResponse>> getOrdersForAdmin() {
        List<OrderResponse> response = orderService.getOrdersForAdmin();
        return ApiResponse.<List<OrderResponse>>builder()
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

    @GetMapping("/status/{status}")
    public ApiResponse<List<OrderResponse>> getOrdersByStatus
            (@PathVariable String status) {
        return ApiResponse.<List<OrderResponse>>builder()
                .data(orderService.getOrdersByStatus(status))
                .build();
    }

    @GetMapping("/{orderCode}")
    public ApiResponse<OrderResponse> getOrderById
            (@PathVariable String orderCode) {
        return ApiResponse.<OrderResponse>builder()
                .data(orderService.getOrderByCode(orderCode))
                .build();
    }
}
