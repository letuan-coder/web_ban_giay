package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.StockTransactionItemResponse;
import com.example.DATN.dtos.respone.StockTransactionResponse;
import com.example.DATN.models.StockTransaction;
import com.example.DATN.models.StockTransactionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {})
public interface StockTransactionMapper {

    @Mapping(target = "variantSku", source = "variant.sku")
    @Mapping(target = "variantId",source = "variant.id")
    StockTransactionItemResponse toStockTransactionItemResponse(StockTransactionItem item);

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "fromStoreId", source = "fromStore.id")
    @Mapping(target = "fromStoreName", source = "fromStore.name")
    @Mapping(target = "toStoreId", source = "toStore.id")
    @Mapping(target = "toStoreName", source = "toStore.name")
    @Mapping(target = "transactionStatus", source = "status")
    @Mapping(target = "fromWarehouseId", source = "fromWareHouse.id")
    @Mapping(target = "fromWarehouseName", source = "fromWareHouse.name")
    @Mapping(target = "toWarehouseId", source = "toWareHouse.id")
    @Mapping(target = "toWarehouseName", source = "toWareHouse.name")
    @Mapping(target = "createdDate",source = "createdAt")
    StockTransactionResponse toStockTransactionResponse(StockTransaction transaction);


}
