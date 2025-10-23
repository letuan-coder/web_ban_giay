package com.example.DATN.repositories;

import com.example.DATN.models.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findById(UUID id);

    Integer countById(UUID id);
}