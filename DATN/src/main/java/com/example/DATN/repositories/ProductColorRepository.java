package com.example.DATN.repositories;

import com.example.DATN.models.Color;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductColorRepository extends JpaRepository<ProductColor, UUID> {
    List<ProductColor> findAllByProduct(Product product);

    ProductColor findByColor(Color color);
    boolean existsByProductAndColor(Product product, Color color);
}