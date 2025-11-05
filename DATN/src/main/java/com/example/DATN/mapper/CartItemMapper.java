package com.example.DATN.mapper;

import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.models.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    @Mapping(source = "cart.id", target = "cartId")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "cart.id", source = "cartId")
    CartItem toEntity(CartItemResponse response);

    @Mapping(target = "productVariant.id", source = "productVariantId")
    CartItem toCartItem(CartItemRequest request);
}
