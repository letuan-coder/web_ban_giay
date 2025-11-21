package com.example.DATN.repositories;

import com.example.DATN.models.OrderReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {
}
