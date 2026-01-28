package com.example.DATN.repositories;

import com.example.DATN.constant.OrderStatus;
import com.example.DATN.models.Order;
import com.example.DATN.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository thao tác dữ liệu đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("""
                SELECT o
                FROM Order o
                WHERE o.ghn.ghnOrderCode IS NOT NULL
                  AND (o.ghnLastSyncAt IS NULL OR o.ghnLastSyncAt < :syncBefore)
                  AND o.ghnFailCount < 10
                  AND o.ghnStatus NOT IN (
                        DELIVERED,
                       CANCEL,
                        RETURNED
                  )
            """)
    List<Order> findOrdersNeedSync(@Param("syncBefore") LocalDateTime syncBefore);


    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndUser_Id(String orderCode, Long userId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findAllByOrderStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findAllByOrderStatusAndUserOrderByCreatedAtDesc(OrderStatus status, User user);

    List<Order> findByUserOrderByCreatedAtDesc(User user);
}

