package com.example.DATN.controllers;

import com.example.DATN.dtos.request.checkout.ApplyVoucherRequest;
import com.example.DATN.dtos.request.checkout.CheckOutRequest;
import com.example.DATN.dtos.request.checkout.DistanceRequest;
import com.example.DATN.dtos.request.checkout.IncreaseQuantityRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.order.CheckOutResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.services.CheckOutService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/check-out")
@RequiredArgsConstructor
public class CheckOutController {
    private final CheckOutService checkOutService;
    @PostMapping
    public ApiResponse<CheckOutResponse> checkOutOrder(
            @RequestBody @Valid CheckOutRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String headerKey
    ) throws JsonProcessingException {
        String idempotencyKey = headerKey != null ? headerKey : request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        request.setIdempotencyKey(idempotencyKey);
        CheckOutResponse response = checkOutService.processCheckoutInternal(request);
        return ApiResponse.<CheckOutResponse>builder()
                .data(response)
                .message(response==null?"Check out ":" check out gì 10p ")
                .build();
    }
    @PostMapping("/check-out-item")
    public ApiResponse<CheckOutResponse> calculateitem(
            @RequestBody @Valid List<IncreaseQuantityRequest> request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey
    ) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        CheckOutResponse response = checkOutService.calculatorItem(request,idempotencyKey);
        return ApiResponse.<CheckOutResponse>builder()
                .data(response)
                .message(response==null?"Check out ":" check out gì 10p ")
                .build();
    }
    @PostMapping("/apply-voucher")
    public ApiResponse<CheckOutResponse> ApplyVoucher (
            @RequestBody @Valid ApplyVoucherRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String headerKey){
        String idempotencyKey = headerKey != null ? headerKey : request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        request.setIdempotencyKey(idempotencyKey);
        CheckOutResponse response = checkOutService.applyVoucher(request);
        if (response == null) {
            throw new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND);
        }
        return ApiResponse.<CheckOutResponse>builder()
                .data(response)
                .message(response==null?"Check out ":" check out gì 10p ")
                .build();
    }

    @PostMapping("/check-out-distance")
    public ApiResponse<CheckOutResponse> calculatorDistance(
            @RequestBody @Valid DistanceRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String headerKey
    ){
        String idempotencyKey = headerKey != null ? headerKey : request.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new ApplicationException(ErrorCode.MISSING_IDEMPOTENCY_KEY);
        }
        request.setIdempotencyKey(idempotencyKey);
        CheckOutResponse response = checkOutService.calculatorDistance(request);
        return ApiResponse.<CheckOutResponse>builder()
                .data(response)
                .build();
    }
    @PostMapping("/delete-key")
    public ApiResponse<Void> deleteIdempotencyKey(
            @RequestHeader(value = "X-Idempotency-Key", required = false)
            String headerKey
    ) {
        checkOutService.DeleteIdempotencyKey(headerKey);
        return ApiResponse.<Void>builder()
                .data(null)
                .message("Idempotency-key is deleted")
                .build();
    }

//    @GetMapping("/ghn-shipping")
//    public ApiResponse<ShippingFeeResponse> deleteIdempotencyKey(
//            @RequestBody Integer id
//    ) {
//        return ApiResponse.<ShippingFeeResponse>builder()
//                .data(checkOutService.calculateGHNfee(id))
//                .message("Idempotency-key is deleted")
//                .build();
//    }
}
