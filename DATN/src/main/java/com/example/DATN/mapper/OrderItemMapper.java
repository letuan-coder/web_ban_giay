package com.example.DATN.mapper;

import com.example.DATN.dtos.request.order.OrderItemRequest;
import com.example.DATN.dtos.respone.order.OrderItemRespone;
import com.example.DATN.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "productColor.id", source = "productColorId")
    OrderItem toOrderItem(OrderItemRequest request);

    @Mapping(source = "productColor.id", target = "productColorId")
    OrderItemRespone toOrderItemRespone(OrderItem orderItem);
}
