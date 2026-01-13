package com.example.DATN.repositories;

import com.example.DATN.constant.StockType;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByVariant_IdAndStore(UUID variantId, Store store);

    Optional<Stock> findByStore_Id(UUID storeId);

    Optional<Stock> findByVariant_IdAndWarehouse(UUID variantId, WareHouse warehouse);

    List<Stock> findByVariant_Id(UUID variant);

    @Query("""
                select s
                from Stock s
                where s.variant.id in :variantIds
                  and s.store = :store
            """)
    List<Stock> findAllByVariantIdsAndStore(
            @Param("variantIds") List<UUID> variantIds,
            @Param("store") Store store
    );

    @Query("""
                select s
                from Stock s
                where s.variant.id in :variantIds
            """)
    List<Stock> findAllByVariantIds(@Param("variantIds") List<UUID> variantIds);

    Optional<Stock> findByStore(Store store);

    Boolean existsByWarehouse_IsCentralAndVariantAndStockType(WareHouse wareHouse, ProductVariant variant, StockType type);

    Boolean existsByStoreAndVariantAndStockType(Store store, ProductVariant variant, StockType type);

    Boolean existsByWarehouseAndVariantAndStockType(WareHouse wareHouse, ProductVariant variant, StockType type);
}
