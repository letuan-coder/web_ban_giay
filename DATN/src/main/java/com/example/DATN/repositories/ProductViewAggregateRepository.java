package com.example.DATN.repositories;

import com.example.DATN.models.ProductViewAggregate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ProductViewAggregateRepository extends JpaRepository<ProductViewAggregate, UUID> {
  Optional<ProductViewAggregate> findByVariantIdAndProductIdAndTimeBucket (UUID productId,UUID variantId, Instant time);

}