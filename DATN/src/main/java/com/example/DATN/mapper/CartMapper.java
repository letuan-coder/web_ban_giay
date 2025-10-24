package com.example.DATN.mapper;

import com.example.DATN.dtos.request.CartRequest;
import com.example.DATN.dtos.respone.CartResponse;
import com.example.DATN.models.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "items",target = "cartItems")
    CartResponse toCartResponse(Cart cart);


    Cart toCart(CartRequest request);
}
