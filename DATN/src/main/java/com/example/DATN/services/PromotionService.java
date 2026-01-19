package com.example.DATN.services;

import com.example.DATN.dtos.request.product.PromotionRequest;
import com.example.DATN.dtos.respone.PromotionPriceResponse;
import com.example.DATN.dtos.respone.product.PromotionResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.PromotionMapper;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Promotion;
import com.example.DATN.repositories.ProductRepository;
import com.example.DATN.repositories.PromotionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final PromotionMapper promotionMapper;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    public PromotionPriceResponse getVariantPrice(String sku) {

        String key = "PROMO:VARIANT:" + sku;

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return (PromotionPriceResponse) value;
    }
    private BigDecimal calculateDiscountPrice(
            BigDecimal originalPrice,
            Promotion promotion
    ) {
        return switch (promotion.getPromotionType()) {
            case  PERCENTAGE ->
                    originalPrice.subtract(
                            originalPrice
                                    .multiply(promotion.getDiscountValue())
                                    .divide(BigDecimal.valueOf(100))
                    );

            case FIXED_AMOUNT ->
                    originalPrice.subtract(promotion.getDiscountValue());

            default -> originalPrice;
        };
    }

    @Transactional
    public void applyPromotion(
            Promotion promotion,
            Set<ProductVariant> variants
    ) throws JsonProcessingException {

        promotionRepository.save(promotion);

        LocalDateTime now = LocalDateTime.now();
        Duration ttl = Duration.between(now, promotion.getEndDate());

        if (ttl.isNegative() || ttl.isZero()) {
            return;
        }

        for (ProductVariant variant : variants) {

            BigDecimal originalPrice = variant.getPrice();
            BigDecimal discountPrice =
                    calculateDiscountPrice(originalPrice, promotion);

            PromotionPriceResponse redisPrice = new PromotionPriceResponse(
                    originalPrice,
                    discountPrice
            );
            String json = objectMapper.writeValueAsString(redisPrice);

            String key = "PROMO:VARIANT:" + variant.getId();

            redisTemplate.opsForValue().set(key, json,ttl);
        }
    }

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
    public PromotionResponse createPromotion(PromotionRequest promotionRequest) throws JsonProcessingException {
        if (promotionRequest.getStartDate().isAfter(promotionRequest.getEndDate())) {
            throw new ApplicationException(ErrorCode.INVALID_PROMOTION_DATES);
        }
        Promotion promotion = promotionMapper.toPromotion(promotionRequest);

        Promotion savedPromotion= promotionRepository.save(promotion);
        LocalDateTime now = LocalDateTime.now();
        Duration ttl = Duration.between(now, promotionRequest.getEndDate());

        if (ttl.isNegative() || ttl.isZero()) {
            return promotionMapper.toPromotionResponse(savedPromotion);
        }
        List<Product> products =
                productRepository.findAllById(promotionRequest.getProductId());

        for (Product product : products) {

            for (ProductColor color : product.getProductColors()) {

                for (ProductVariant variant : color.getVariants()) {

                    BigDecimal originalPrice = variant.getPrice();
                    BigDecimal discountPrice =
                            calculateDiscountPrice(
                                    originalPrice, savedPromotion
                            );

                    PromotionPriceResponse redisPrice =
                            new PromotionPriceResponse(
                                    originalPrice,
                                    discountPrice
                            );

                    String key =
                            "PROMO:VARIANT:" + variant.getSku();
                    String json = objectMapper.writeValueAsString(redisPrice);
                    redisTemplate.opsForValue()
                            .set(key,json ,
                                    ttl.getSeconds(),
                                    TimeUnit.SECONDS);
                }
            }
        }
        return promotionMapper.toPromotionResponse(savedPromotion);
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
//        promotion.setDiscountValue(promotionRequest.getDiscountValue());
        promotion.setStartDate(promotionRequest.getStartDate());
        promotion.setEndDate(promotionRequest.getEndDate());
        promotion.setActive(promotionRequest.getActive());
        promotion.setPromotionType(promotionRequest.getPromotionType());
//        if (promotionRequest.getProductId() != null && !promotionRequest.getProductId().isEmpty()) {
//            Set<ProductVariant> productVariants = new HashSet<>(productVariantRepository
//                    .findAllById(promotionRequest.getProductId()));
//            promotion.setProductVariants(productVariants);
//        } else {
//            promotion.getProductVariants().clear();
//        }
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    public PromotionResponse addProductVariantsToPromotion(
            Long promotionId, Set<UUID> productVariantIds) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PROMOTION_NOT_FOUND));
//        Set<ProductVariant> productVariants = new HashSet<>(productVariantRepository.findAllById(productVariantIds));
//        promotion.getProductVariants().addAll(productVariants);
        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }
}