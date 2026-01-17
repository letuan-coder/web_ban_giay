package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.StockResponse;
import com.example.DATN.models.Stock;
import com.example.DATN.models.WareHouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {StoreMapper.class,
                WareHouse.class})
public interface StockMapper {

    @Mapping(source = "variant.sku", target = "sku")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "warehouse.id", target = "warehouseId")
//    @Mapping(source = "createdAt",target = "actualReceivedDate")
    StockResponse toStockResponse(Stock stock);

    @Mapping(target = "variant.sku", source = "sku")
    @Mapping(target = "store.id", source = "storeId")
    @Mapping(target = "warehouse.id", source = "warehouseId")
//    @Mapping(source = "createdAt",target = "actualReceivedDate")
    Stock ToStock(StockResponse stock);
}
