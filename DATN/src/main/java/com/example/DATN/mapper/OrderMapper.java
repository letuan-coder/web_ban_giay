package com.example.DATN.mapper;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.models.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {OrderItemMapper.class,UserAddressMapper.class})
public interface OrderMapper {
    @Mapping(target = "paymentMethod",source = "type")
    @Mapping(target = "items",source = "orderItemRequests")
    Order toEntity (OrderRequest request);


    @Mapping(target = "paymentMethodName",source = "paymentMethod")
    @Mapping(target = "totalPrice",source = "total_price")
    @Mapping(target = "items",source = "items")
    @Mapping(target = "created_At",source = "createdAt")
    @Mapping(target = "userAddress",source = "userAddress.userAddress")
    @Mapping(target = "receiverName",source = "userAddress.receiverName")
    @Mapping(target = "phoneNumber",source = "userAddress.phoneNumber")
    @Mapping(target = "userName",source = "user.username")
    OrderResponse toResponse(Order oder);

}
