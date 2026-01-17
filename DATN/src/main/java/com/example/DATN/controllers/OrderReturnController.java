package com.example.DATN.controllers;

import com.example.DATN.dtos.request.OrderReturnRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.OrderReturnResponse;
import com.example.DATN.services.OrderReturnService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order-returns")
@RequiredArgsConstructor
public class OrderReturnController {
    private final OrderReturnService orderReturnService;
    private final ObjectMapper objectMapper;
    @PostMapping(value = "/{orderId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OrderReturnResponse> createOrderReturnRequest(
            @PathVariable UUID orderId,
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false)
            @RequestParam(required = false)
            MultiValueMap<String, MultipartFile> files) throws JsonProcessingException {
        OrderReturnRequest request =
                objectMapper.readValue(requestJson, OrderReturnRequest.class);
        request.setOrderId(orderId);

        OrderReturnResponse response = orderReturnService.createReturnRequest(request,files);
        return ApiResponse.<OrderReturnResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<OrderReturnResponse>> getAllReturnRequest(){
        return ApiResponse.<List<OrderReturnResponse>>builder()
                .data(orderReturnService.getAllOrderReturnRequest())
                .build();
    }


}
