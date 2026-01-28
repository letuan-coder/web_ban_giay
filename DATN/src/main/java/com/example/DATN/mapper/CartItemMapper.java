package com.example.DATN.mapper;

import com.example.DATN.dtos.request.cart.CartItemRequest;
import com.example.DATN.dtos.respone.cart.CartItemResponse;
import com.example.DATN.dtos.respone.cart.ProductCartItemResponse;
import com.example.DATN.models.CartItem;
import com.example.DATN.models.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    @Mapping(source = "cart.id", target = "cartId")
    @Mapping(target = "productVariant",source = "productVariant")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "cart.id", source = "cartId")
    @Mapping(target = "productVariant.size", ignore = true)
    CartItem toEntity(CartItemResponse response);

    @Mapping(target = "productVariant.sku", source = "sku")
    @Mapping(target = "productVariant.size", ignore = true)
    CartItem toCartItem(CartItemRequest request);

    @Mapping(target = "sku",source = "productVariant.sku")
    @Mapping(target = "color",source = "productVariant.productColor.color.name")
    @Mapping(target = "size",source = "productVariant.size.name")
    @Mapping(target = "thumbnailUrl",source = "productVariant.productColor.product.thumbnailUrl")
    @Mapping(target = "name",source = "productVariant.productColor.product.name")
    @Mapping(target = "price",source = "productVariant.price")
    ProductCartItemResponse toProductCartItem(CartItem cartItem);


    @Mapping(target = "name", source = "productColor.product.name")
    @Mapping(target = "sku", source = "sku")
    @Mapping(target = "thumbnailUrl", source = "productColor.product.thumbnailUrl")
    @Mapping(target = "color", source = "productColor.color.name")
    @Mapping(target = "size", source = "size.name")
    @Mapping(target = "price", source = "price")
    ProductCartItemResponse toProductCartItem(ProductVariant productVariant);
}

