package com.example.DATN.mapper;

import com.example.DATN.dtos.request.product.PromotionRequest;
import com.example.DATN.dtos.respone.product.PromotionResponse;
import com.example.DATN.models.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "productVariants", ignore = true)
    Promotion toPromotion(PromotionRequest request);

    PromotionResponse toPromotionResponse(Promotion promotion);
}
