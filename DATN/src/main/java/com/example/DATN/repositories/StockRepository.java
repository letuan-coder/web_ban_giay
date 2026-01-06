package com.example.DATN.repositories;

import com.example.DATN.constant.StockType;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByVariantAndStore(ProductVariant variant, Store store);

    Optional<Stock> findByStore_Id(UUID storeId);

    Optional<Stock> findByVariantAndWarehouse(ProductVariant variant, WareHouse warehouse);

    List<Stock> findByVariant(ProductVariant variant);

    Optional<Stock> findByStore(Store store);

    Boolean existsByStoreAndVariantAndStockType(Store store, ProductVariant variant, StockType type);
    Boolean existsByWarehouseAndVariantAndStockType(WareHouse wareHouse, ProductVariant variant, StockType type);
}
