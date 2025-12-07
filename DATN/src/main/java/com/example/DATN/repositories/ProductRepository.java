package com.example.DATN.repositories;

import com.example.DATN.models.Product;
import com.example.DATN.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> {
    List<Product> findByProductCode(String productCode);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findBySupplier(Supplier supplier);
}