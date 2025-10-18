package com.example.DATN.repositories;

import com.example.DATN.models.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository thao tác dữ liệu phương thức thanh toán
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
}

