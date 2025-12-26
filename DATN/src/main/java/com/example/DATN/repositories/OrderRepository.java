package com.example.DATN.repositories;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.models.Order;
import com.example.DATN.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
   Optional<Order> findByOrderCode(String orderCode);
   List<Order> findAllByOrderStatus(OrderStatus status);
   List<Order> findAllByOrderStatusAndUser(OrderStatus status, User user);
   List<Order> findByUser(User user);
}

