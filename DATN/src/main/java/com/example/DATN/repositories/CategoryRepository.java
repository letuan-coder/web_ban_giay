package com.example.DATN.repositories;

import com.example.DATN.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu danh mục sản phẩm
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByNameContainingIgnoreCase(String name);
}

