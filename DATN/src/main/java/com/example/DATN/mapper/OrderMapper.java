package com.example.DATN.mapper;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderRespone;
import com.example.DATN.models.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class})
public interface OrderMapper {
    Order toEntity (OrderRequest request);

    @Mapping(target = "paymentMethodName",source = "paymentMethod.displayName")
    @Mapping(target = "totalPrice",source = "total_price")
    @Mapping(target = "items",source = "items")
    @Mapping(target = "created_At",source = "createdAt")
    OrderRespone toResponse(Order oder);

}
