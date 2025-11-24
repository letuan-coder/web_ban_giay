package com.example.DATN.repositories;

import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByVariantAndStore(ProductVariant variant, Store store);
    Optional<Stock> findByVariantAndWarehouse(ProductVariant variant, WareHouse warehouse);
}
