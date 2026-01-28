package com.example.DATN.controllers;

import com.example.DATN.dtos.request.ProductReviewRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.services.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @PostMapping
            ("/{orderCode}")
    public ApiResponse<Void> createProductReview(
            @PathVariable String orderCode,
            @RequestBody @Valid ProductReviewRequest request
    ){
        productReviewService.AddReview(
                orderCode,
                request);
        return ApiResponse.<Void>builder()
                .data(null)
                .message("rating product success")
                .build();
    }
}
