package com.example.DATN.repositories;

import com.example.DATN.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu sản phẩm trong đơn hàng
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}

