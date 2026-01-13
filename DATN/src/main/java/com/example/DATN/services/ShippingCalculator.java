package com.example.DATN.services;


import com.example.DATN.constant.WeightTier;
import com.example.DATN.dtos.respone.order.CheckOutResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.StoreRepository;
import com.example.DATN.repositories.projection.CheckOutProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShippingCalculator {
    private final ProductVariantRepository productVariantRepository;
    private final BigDecimal BASE_SHIPPING_FEE = BigDecimal.valueOf(15);
    private final BigDecimal BASE_FEE_PER_ITEM = BigDecimal.ONE;
    private final StoreRepository storeRepository;
//    @Cacheable(
//            value = "product_variant",
//            key = "#sku",
//            unless = "#result == null"
//    )
//    public ProductVariant getBySku(String sku) {
//        return productVariantRepository.findBysku(sku)
//                .orElseThrow(() -> new ApplicationException(
//                        ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//    }
    public BigDecimal calculatorShippingFee(Map<String,Integer> variants
            , CheckOutProjection store, Integer quantity, Double Distance, CheckOutResponse response) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalPriceWeight = BigDecimal.ZERO;
        BigDecimal totalPriceDistance = BigDecimal.ZERO;
        if (store != null) {
            totalPriceDistance = calculateShippingFeeByDistance(Distance);
            response.setDistanceFee(totalPriceDistance);
        }
        totalPriceWeight = calculatorShippingFeeByWeight(variants);
        response.setWeightFee(totalPriceWeight);
        BigDecimal priceQuantity = BASE_FEE_PER_ITEM.multiply(BigDecimal.valueOf(quantity*1000));
        response.setQuantityFee(priceQuantity);
        totalPrice = totalPrice.add(totalPriceDistance.add(totalPriceWeight).add(priceQuantity));
        return totalPrice;
    }
    public BigDecimal calculatorShippingFeeByWeight (Map<String,Integer> variants){
        BigDecimal totalPrice = BigDecimal.ZERO;
        Integer totalWeight = 0;
        for (Map.Entry<String,Integer> Productvariant : variants.entrySet()) {
            Integer quantity = Productvariant.getValue();
            String sku = Productvariant.getKey();
            ProductVariant variant = productVariantRepository.findBysku(sku)
                    .orElseThrow(()->new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
            totalWeight += variant.getWeight() * quantity;
        }
        WeightTier weightTier = WeightTier.fromWeight(totalWeight);
        switch (weightTier) {
            case FREE_WEIGHT -> {
                totalPrice = BigDecimal.ZERO;
            }
            case LIGHT -> {
                totalPrice =WeightTier.LIGHT.getExtraFee();
            }
            case MEDIUM -> {
                totalPrice = WeightTier.MEDIUM.getExtraFee();
            }
            case HEAVY -> {
                totalPrice =WeightTier.HEAVY.getExtraFee();
            }
            case VERY_HEAVY -> {
                totalPrice = WeightTier.VERY_HEAVY.getExtraFee();
            }
        }return totalPrice;
    }

        public BigDecimal calculateShippingFeeByDistance(double distanceKm) {
            int category;

            if (distanceKm >= 0 && distanceKm < 3) {
                category = 0;
            } else if (distanceKm >= 3 && distanceKm < 7) {
                category = 1;
            } else if (distanceKm >= 7 && distanceKm < 10) {
                category = 2;
            } else if (distanceKm >= 10 && distanceKm < 20) {
                category = 3;
            } else {
                category = 4;
            }
            switch (category) {
                case 0:
                    return BigDecimal.valueOf(15_000);
                case 1:
                    return BigDecimal.valueOf(30_000);
                case 2:
                    return BigDecimal.valueOf(50_000);
                case 3:
                    return BigDecimal.valueOf(100_000);
                default:
                    return BigDecimal.valueOf(150_000);
            }

    }
}
