package com.example.DATN.repositories;

import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findAllByProductVariants(ProductVariant productVariant);
}