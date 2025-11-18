package com.example.DATN.services;

import com.example.DATN.dtos.request.product.PromotionRequest;
import com.example.DATN.dtos.respone.product.PromotionResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.PromotionMapper;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Promotion;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private PromotionMapper promotionMapper;

    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(promotionMapper::toPromotionResponse)
                .collect(Collectors.toList());

    }

    public Optional<PromotionResponse> getPromotionById(Long id) {
        return promotionRepository.findById(id)
                .map(promotionMapper::toPromotionResponse);
    }
    @Transactional(rollbackFor = Exception.class)
    public PromotionResponse createPromotion(PromotionRequest promotionRequest) {
        if(promotionRequest.getStartDate().isAfter(promotionRequest.getEndDate())) {
            throw new ApplicationException(ErrorCode.INVALID_PROMOTION_DATES);
        }
        Promotion promotion = promotionMapper.toPromotion(promotionRequest);
        if (promotionRequest.getProductVariantIds() != null && !promotionRequest.getProductVariantIds().isEmpty()) {
            Set<ProductVariant> productVariants = new HashSet<>(productVariantRepository.findAllById(promotionRequest.getProductVariantIds()));
            promotion.setProductVariants(productVariants);
        }
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }
    @Transactional(rollbackFor = Exception.class)
    public PromotionResponse updatePromotion(Long id, PromotionRequest promotionRequest) {
        if (promotionRequest.getStartDate().isAfter(promotionRequest.getEndDate())) {
            throw new ApplicationException(ErrorCode.INVALID_PROMOTION_DATES);
        }
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));
        promotion.setName(promotionRequest.getName());
        promotion.setDescription(promotionRequest.getDescription());
        promotion.setDiscountValue(promotionRequest.getDiscountValue());
        promotion.setStartDate(promotionRequest.getStartDate());
        promotion.setEndDate(promotionRequest.getEndDate());
        promotion.setActive(promotionRequest.getActive());
        promotion.setPromotionType(promotionRequest.getPromotionType());
        if (promotionRequest.getProductVariantIds() != null && !promotionRequest.getProductVariantIds().isEmpty()) {
            Set<ProductVariant> productVariants = new HashSet<>(productVariantRepository.findAllById(promotionRequest.getProductVariantIds()));
            promotion.setProductVariants(productVariants);
        } else {
            promotion.getProductVariants().clear();
        }
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }
    public PromotionResponse addProductVariantsToPromotion(
            Long promotionId, Set<UUID> productVariantIds) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));
        Set<ProductVariant> productVariants = new HashSet<>(productVariantRepository.findAllById(productVariantIds));
        promotion.getProductVariants().addAll(productVariants);
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }
    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }
}