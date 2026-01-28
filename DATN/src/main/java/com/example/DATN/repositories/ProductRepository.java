package com.example.DATN.repositories;

import com.example.DATN.models.Product;
import com.example.DATN.models.Supplier;
import com.example.DATN.repositories.projection.ProductSalesProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>,
        JpaSpecificationExecutor<Product> {
    Boolean existsBySupplier(Supplier supplier);
    @Modifying
    @Query("UPDATE Product p SET p.totalView = p.totalView + :increment WHERE p.id = :productId")
    void incrementTotalView(@Param("productId") UUID productId,
                            @Param("increment") Long increment);
    @Query("SELECT p.id as productId, p.name as productName," +
            "p.productCode as productCode, " +
            "p.thumbnailUrl as thumbnailUrl , p.price as price, " +
            "SUM(oi.quantity) as totalSold " +
            "FROM Product p " +
            "JOIN ProductColor pc ON pc.product.id = p.id " +
            "JOIN ProductVariant pv ON pv.productColor.id = pc.id " +
            "JOIN OrderItem oi ON oi.productVariant.id = pv.id " +
            "JOIN Order o ON oi.order.id = o.id " +
            "WHERE o.orderStatus = 'COMPLETED' " +
            "GROUP BY p.id, p.name, p.productCode, p.thumbnailUrl, p.price " +
            "ORDER BY totalSold DESC")
    List<ProductSalesProjection> findWorstSellingProducts(Pageable pageable);

    @Query("SELECT p.id as productId, " +
            "p.name as productName, " +
            "p.productCode as productCode, " +
            "p.thumbnailUrl as thumbnailUrl, " +
            "p.price as price, " +
            "p.createdAt as createdAt, " +
            "SUM(oi.quantity) as totalSold, " +
            "SUM(oi.quantity * oi.price) as totalRevenue " +
            "FROM Product p " +
            "JOIN ProductColor pc ON pc.product.id = p.id " +
            "JOIN ProductVariant pv ON pv.productColor.id = pc.id " +
            "JOIN OrderItem oi ON oi.productVariant.id = pv.id " +
            "JOIN Order o ON oi.order.id = o.id " +
            "WHERE o.orderStatus = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY p.id, p.name, p.productCode, p.thumbnailUrl, p.price, p.createdAt " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<ProductSalesProjection> findBestSellingProductsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    @Query("SELECT p.id as productId, " +
            "p.name as productName, " +
            "p.productCode as productCode, " +
            "p.thumbnailUrl as thumbnailUrl, " +
            "p.price as price, " +
            "p.createdAt as createdAt, " +
            "b.name as brandName, " +
            "c.name as categoryName, " +
            "SUM(oi.quantity) as totalSold, " +
            "SUM(oi.quantity * oi.price) as totalRevenue " +
            "FROM Product p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN p.category c " +
            "JOIN ProductColor pc ON pc.product.id = p.id " +
            "JOIN ProductVariant pv ON pv.productColor.id = pc.id " +
            "JOIN OrderItem oi ON oi.productVariant.id = pv.id " +
            "JOIN Order o ON oi.order.id = o.id " +
            "WHERE o.orderStatus = 'COMPLETED' " +
            "GROUP BY p.id, p.name, p.productCode, p.thumbnailUrl, p.price, p.createdAt, b.name, c.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    Page<ProductSalesProjection> findBestSellingProductsDetail(Pageable pageable);

    List<Product> findAllBySupplier(Supplier supplier);

    List<Product> findByProductCode(String productCode);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findBySupplier(Supplier supplier);
}