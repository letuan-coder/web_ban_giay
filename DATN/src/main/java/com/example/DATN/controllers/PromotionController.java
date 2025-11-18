package com.example.DATN.controllers;

import com.example.DATN.dtos.request.product.PromotionRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.product.PromotionResponse;
import com.example.DATN.services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ApiResponse<List<PromotionResponse>> getAllPromotions() {
        return ApiResponse.<List<PromotionResponse>>builder()
                .data(promotionService.getAllPromotions())
                .message("Success")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PromotionResponse> getPromotionById(@PathVariable Long id) {
        return promotionService.getPromotionById(id)
                .map(promotion -> ApiResponse.<PromotionResponse>builder()
                        .data(promotion)
                        .message("Success")
                        .build())
                .orElse(ApiResponse.<PromotionResponse>builder()
                        .message("Not Found")
                        .build());
    }

    @PostMapping
    public ApiResponse<PromotionResponse> createPromotion(@RequestBody PromotionRequest promotionRequest) {
        return ApiResponse.<PromotionResponse>builder()
                .data(promotionService.createPromotion(promotionRequest))
                .message("Promotion created successfully")
                .build();
    }

    @PostMapping("/bulk")
    public ApiResponse<List<PromotionResponse>> createPromotionsBulk
            (@RequestBody List<PromotionRequest> promotionRequests) {
        List<PromotionResponse> createdPromotions = promotionRequests.stream()
                .map(promotionService::createPromotion)
                .toList();
        return ApiResponse.<List<PromotionResponse>>builder()
                .data(createdPromotions)
                .message("Promotions created successfully")
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<PromotionResponse> updatePromotion(@PathVariable Long id, @RequestBody PromotionRequest promotionRequest) {
        try {
            PromotionResponse updatedPromotion = promotionService.updatePromotion(id, promotionRequest);
            return ApiResponse.<PromotionResponse>builder()
                    .data(updatedPromotion)
                    .message("Promotion updated successfully")
                    .build();
        } catch (RuntimeException e) {
            return ApiResponse.<PromotionResponse>builder()
                    .message("Not Found")
                    .build();
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ApiResponse.<Void>builder()
                .message("Promotion deleted successfully")
                .build();
    }

    @PostMapping("/{id}/variants")
    public ApiResponse<PromotionResponse> addVariantsToPromotion(
            @PathVariable Long id,
            @RequestBody Set<UUID> productVariantIds) {
        try {
            PromotionResponse updatedPromotion = promotionService.addProductVariantsToPromotion(id, productVariantIds);
            return ApiResponse.<PromotionResponse>builder()
                    .data(updatedPromotion)
                    .message("Variants added to promotion successfully.")
                    .build();
        } catch (RuntimeException e) {
            return ApiResponse.<PromotionResponse>builder()
                    .message(e.getMessage())
                    .build();
        }
    }

//    @DeleteMapping("/{id}/variants")
//    public ApiResponse<PromotionResponse> removeVariantsFromPromotion(
//            @PathVariable Long id,
//            @RequestBody List<UUID> productVariantIds) {
//        try {
//            PromotionResponse updatedPromotion = promotionService.removeVariantsFromPromotion(id, productVariantIds);
//            return ApiResponse.<PromotionResponse>builder()
//                    .data(updatedPromotion)
//                    .message("Variants removed from promotion successfully.")
//                    .build();
//        } catch (RuntimeException e) {
//            return ApiResponse.<PromotionResponse>builder()
//                    .message(e.getMessage())
//                    .build();
//        }
//    }
}
