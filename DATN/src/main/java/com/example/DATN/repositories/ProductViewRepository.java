package com.example.DATN.repositories;

import com.example.DATN.models.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductViewRepository extends JpaRepository<ProductView, UUID> {
}