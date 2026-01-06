package com.example.DATN.repositories;

import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findAllByproductColor(ProductColor productColor);

    ProductVariant findByProductColor (ProductColor productColor);

    Optional<ProductVariant> findById(UUID id);


    Integer countById(UUID id);

    Optional<ProductVariant> findBysku(String sku);
    @Modifying
    @Transactional
    @Query(
            value = """
        UPDATE product_variant pv
        LEFT JOIN stock s ON s.variant_id = pv.id
        SET pv.is_available = 'NOT_AVAILABLE'
        WHERE s.quantity IS NULL OR s.quantity <= 0
    """, nativeQuery = true
    )
    int setNotAvailableIfOutOfStockNative();

    @Modifying
    @Transactional
    @Query(
            value = """
            UPDATE product_variant pv
            JOIN stock s ON s.variant_id = pv.id
            SET pv.is_available = 'AVAILABLE'
            WHERE s.quantity > 0
        """,
            nativeQuery = true
    )
    int setAvailableIfInStockNative();
}