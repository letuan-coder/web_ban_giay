
package com.example.DATN.repositories;

import com.example.DATN.models.ImageProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageProductRepository extends JpaRepository<ImageProduct, UUID> {
}
