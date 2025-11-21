package com.example.DATN.repositories;

import com.example.DATN.models.Vnpay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VnpayRepository extends JpaRepository<Vnpay, UUID> {
    @Query(value = "SELECT SUM(vnp_amount) FROM vnpay WHERE LEFT(vnp_pay_date, 8) = DATE_FORMAT(CURDATE(), '%Y%m%d')", nativeQuery = true)
    Long sumRevenueToday();

    @Query(value = "SELECT SUM(vnp_amount) FROM vnpay WHERE LEFT(vnp_pay_date, 6) = DATE_FORMAT(CURDATE(), '%Y%m')", nativeQuery = true)
    Long sumRevenueThisMonth();

    @Query(value = "SELECT SUM(vnp_amount) FROM vnpay WHERE LEFT(vnp_pay_date, 4) = YEAR(CURDATE())", nativeQuery = true)
    Long sumRevenueThisYear();

    Optional<Vnpay> findByVnpTxnRef(String vnpTxnRef);
}