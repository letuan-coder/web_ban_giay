package com.example.DATN.repositories;

import com.example.DATN.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
}

