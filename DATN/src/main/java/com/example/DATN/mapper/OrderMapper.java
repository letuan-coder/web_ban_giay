package com.example.DATN.mapper;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderResponse;
import com.example.DATN.dtos.respone.order.PendingOrderRedis;
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

    @Mapping(target = "orderCode", source = "orderCode")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "totalPrice", source = "total_price")
    @Mapping(target = "totalWeight", source = "total_weight")
    @Mapping(target = "totalHeight", source = "total_height")
    @Mapping(target = "totalWidth", source = "total_width")
    @Mapping(target = "totalLength", source = "total_length")
    @Mapping(target = "items", source = "items")
    PendingOrderRedis toPendingOrderRedis(Order order);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderCode", source = "orderCode")

    @Mapping(target = "total_price", source = "totalPrice")
    @Mapping(target = "total_weight", source = "totalWeight")
    @Mapping(target = "total_height", source = "totalHeight")
    @Mapping(target = "total_width", source = "totalWidth")
    @Mapping(target = "total_length", source = "totalLength")
    @Mapping(target = "items", source = "items")
    Order toOrder(PendingOrderRedis pending);
}
