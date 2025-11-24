package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.StockTransactionItemResponse;
import com.example.DATN.dtos.respone.StockTransactionResponse;
import com.example.DATN.models.StockTransaction;
import com.example.DATN.models.StockTransactionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StockTransactionMapper {

    @Mapping(target = "variantSku", source = "variant.sku")
    StockTransactionItemResponse toStockTransactionItemResponse(StockTransactionItem item);

    default StockTransactionResponse toStockTransactionResponse(StockTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        List<StockTransactionItemResponse> itemResponses = transaction.getItems().stream()
                .map(this::toStockTransactionItemResponse)
                .collect(Collectors.toList());

        return StockTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .supplierId(transaction.getSupplier() != null ? transaction.getSupplier().getId() : null)
                .supplierName(transaction.getSupplier() != null ? transaction.getSupplier().getName() : null)
                .fromWarehouseId(transaction.getFromWareHouse() != null ? transaction.getFromWareHouse().getId() : null)
                .fromWarehouseName(transaction.getFromWareHouse() != null ? transaction.getFromWareHouse().getName() : null)
                .fromStoreId(transaction.getFromStore() != null ? transaction.getFromStore().getId() : null)
                .fromStoreName(transaction.getFromStore() != null ? transaction.getFromStore().getName() : null)
                .toWarehouseId(transaction.getToWareHouse() != null ? transaction.getToWareHouse().getId() : null)
                .toWarehouseName(transaction.getToWareHouse() != null ? transaction.getToWareHouse().getName() : null)
                .toStoreId(transaction.getToStore() != null ? transaction.getToStore().getId() : null)
                .toStoreName(transaction.getToStore() != null ? transaction.getToStore().getName() : null)
                .createdDate(transaction.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
