package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.ProductReviewResponse;
import com.example.DATN.models.ProductReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {
        UserMapper.class, SizeMapper.class,
                        ColorMapper.class,
                        OrderItemMapper.class,
                        ProductVariantMapper.class})
public interface ProductReviewMapper {
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "size", source = "orderItem.productVariant.size.name")
    @Mapping(target = "color", source = "orderItem.productVariant.productColor.color.name")
    @Mapping(target = "createdAt", source = "createdAt")
    ProductReviewResponse toResponse(ProductReview productReview);
}
