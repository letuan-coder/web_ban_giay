package com.example.DATN.repositories;

import com.example.DATN.models.OrderReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReturnItemRepository extends JpaRepository<OrderReturnItem, Long> {
}
