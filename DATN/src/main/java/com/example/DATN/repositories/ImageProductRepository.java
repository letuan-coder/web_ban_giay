
package com.example.DATN.repositories;

import com.example.DATN.models.ImageProduct;
import com.example.DATN.models.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageProductRepository extends JpaRepository<ImageProduct, UUID> {
    List<ImageProduct> findAllByProductColor(ProductColor productColor);
    List<ImageProduct> findAllByProductColor_Id(UUID productColor);

}
