package com.example.DATN.mapper;

import com.example.DATN.dtos.request.order.OrderRequest;
import com.example.DATN.dtos.respone.order.OrderRespone;
import com.example.DATN.models.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toEntity (OrderRequest request);

    OrderRespone toResponse(Order oder);

}
