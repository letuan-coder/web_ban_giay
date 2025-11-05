package com.example.DATN.repositories;

import com.example.DATN.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu sản phẩm
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List <Product> findAllByProductCode(String productCode);
    Optional<Product> findByProductCode(String productCode);

    boolean existsByName(String name);
}

