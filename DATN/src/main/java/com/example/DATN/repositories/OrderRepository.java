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

    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndUser_Id(String orderCode,Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findAllByOrderStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findAllByOrderStatusAndUserOrderByCreatedAtDesc(OrderStatus status, User user);

    List<Order> findByUserOrderByCreatedAtDesc(User user);
}

