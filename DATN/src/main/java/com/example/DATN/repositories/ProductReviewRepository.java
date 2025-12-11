package com.example.DATN.repositories;

import com.example.DATN.models.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
}