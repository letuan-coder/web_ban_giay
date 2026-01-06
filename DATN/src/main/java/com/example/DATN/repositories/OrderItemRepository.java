package com.example.DATN.repositories;

import com.example.DATN.models.Order;
import com.example.DATN.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu sản phẩm trong đơn hàng
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findAllByOrder(Order order);
    Boolean existsByOrder_Id(UUID id);
}

