package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.models.Stock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMapper {

    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    StockResponse toStockResponse(Stock stock);
}
