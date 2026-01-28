package com.example.DATN.repositories;

import com.example.DATN.constant.StockType;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findByVariant_IdAndStore(UUID variantId, Store store);

    Optional<Stock> findByStore_Id(UUID storeId);


    @Query("""
                SELECT s
                FROM Stock s
                WHERE s.variant.sku IN :skus
            """)
    List<Stock> findAllBySkus(@Param("skus") List<String> skus);

    @Modifying
    @Query("""
                UPDATE Stock s
                SET s.sellableQuantity = s.sellableQuantity + :qty,
                    s.lockedQuantity   = s.lockedQuantity - :qty
                WHERE s.id = :stockId
                  AND s.lockedQuantity >= :qty
            """)
    int unlockStock(
            @Param("stockId") UUID stockId,
            @Param("qty") int qty
    );

    @Modifying
    @Query("""
                UPDATE Stock s
                SET s.sellableQuantity = s.sellableQuantity - :qty,
                    s.lockedQuantity   = s.lockedQuantity + :qty
                WHERE s.id = :stockId
                  AND s.sellableQuantity >= :qty
            """)
    int lockStock(
            @Param("stockId") UUID stockId,
            @Param("qty") int qty
    );


    @Modifying
    @Query("""
                UPDATE Stock s
                SET s.quantity        = s.quantity - :qty,
                    s.lockedQuantity  = s.lockedQuantity - :qty
                WHERE s.id = :stockId
                  AND s.lockedQuantity >= :qty
                  AND s.quantity >= :qty
            """)
    int commitStock(
            @Param("stockId") UUID stockId,
            @Param("qty") int qty
    );

    Optional<Stock> findByVariant_IdAndWarehouse(UUID variantId, WareHouse warehouse);
    List<Stock> findByVariant_IdInAndWarehouse(
            List<UUID> variantIds,
            WareHouse warehouse
    );

    @Query("""
                select s
                from Stock s
                where s.variant.id in :variantIds
                  and s.warehouse = :warehouse
            """)
    List<Stock> findAllByVariantIdsAndWarehouse(
            @Param("variantIds") List<UUID> variantIds,
            @Param("warehouse") WareHouse wareHouse);

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
                AND s.stockType = StockType.STORE
            """)
    List<Stock> findAllByVariantIdsAndStockType(@Param("variantIds") List<UUID> variantIds);

    Optional<Stock> findByStore(Store store);

    Boolean existsByWarehouse_IsCentralAndVariantAndStockType(WareHouse wareHouse, ProductVariant variant, StockType type);

    Boolean existsByStoreAndVariantAndStockType(Store store, ProductVariant variant, StockType type);

    Boolean existsByWarehouseAndVariantAndStockType(WareHouse wareHouse, ProductVariant variant, StockType type);
}
