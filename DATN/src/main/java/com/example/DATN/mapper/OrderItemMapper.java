package com.example.DATN.mapper;

import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.respone.order.OrderItemRespone;
import com.example.DATN.models.ImageProduct;
import com.example.DATN.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring",uses = {ProductVariantMapper.class})
public interface OrderItemMapper {
    OrderItem toEntity(OrderItemRespone respone);

    OrderItem toOrderItem(OrderItemRequest request);

    @Mapping(target = "productName",source = "productVariant.productColor.product.name")
    @Mapping(target = "discountPrice",source = "productVariant.discountPrice")
    @Mapping(target = "colorName",source = "productVariant.productColor.color.name")
    @Mapping(target = "sizeName",source = "productVariant.size.name")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "image", source = "productVariant.productColor.images", qualifiedByName = "getFirstImageUrl")
    OrderItemRespone toOrderItemRespone(OrderItem orderItem);

    @Named("getFirstImageUrl")
    default String getFirstImageUrl(List<ImageProduct> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.get(0).getImageUrl();
    }
}
