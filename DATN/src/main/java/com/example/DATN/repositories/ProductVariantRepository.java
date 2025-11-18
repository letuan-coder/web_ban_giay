package com.example.DATN.repositories;

import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
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
}