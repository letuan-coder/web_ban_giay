package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.OrderReturnResponse;
import com.example.DATN.dtos.respone.ReturnItemResponse;
import com.example.DATN.models.OrderReturn;
import com.example.DATN.models.OrderReturnItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderReturnMapper {

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "user.username", target = "userName")
    OrderReturnResponse toOrderReturnResponse(OrderReturn orderReturn);

    @Mapping(source = "orderItem.id", target = "orderItemId")
    @Mapping(source = "orderItem.productVariant.size.name", target = "productVariantName") // Or color, adjust as needed
    @Mapping(source = "orderItem.price", target = "price")
    ReturnItemResponse toReturnItemResponse(OrderReturnItem orderReturnItem);

    default List<ReturnItemResponse> toReturnItemResponseList(List<OrderReturnItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toReturnItemResponse)
                .collect(Collectors.toList());
    }
}
