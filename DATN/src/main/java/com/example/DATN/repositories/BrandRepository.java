package com.example.DATN.repositories;

import com.example.DATN.models.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu thương hiệu
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByNameContainingIgnoreCase(String name);
}

