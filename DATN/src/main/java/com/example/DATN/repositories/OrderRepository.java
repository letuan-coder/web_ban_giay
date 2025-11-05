package com.example.DATN.repositories;

import com.example.DATN.models.Order;
import com.example.DATN.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository thao tác dữ liệu đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
   List<Order> findByUser(User user);
}

