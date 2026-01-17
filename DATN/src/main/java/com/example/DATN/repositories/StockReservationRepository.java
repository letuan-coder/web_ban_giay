package com.example.DATN.repositories;

import com.example.DATN.constant.StockReservationStatus;
import com.example.DATN.models.StockReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    @Modifying
    @Query("""
                UPDATE StockReservation r
                SET r.status = 'COMMITTED'
                WHERE r.orderCode = :orderCode
                  AND r.status = 'HOLD'
            """)
    int markCommitted(@Param("orderCode") String orderCode);

    @Modifying
    @Query("""
                UPDATE StockReservation r
                SET r.status = 'RELEASED'
                WHERE r.id = :id
                  AND r.status = 'HOLD'
            """)
    int markExpiredReleased(@Param("id") UUID id);

    @Query("""
                SELECT r
                FROM StockReservation r
                WHERE r.status = 'HOLD'
                  AND r.expiresAt <= CURRENT_TIMESTAMP
            """)
    List<StockReservation> findExpiredReservations();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                select r from StockReservation r
                where r.orderCode = :orderCode
                  and r.status = :status
            """)
    List<StockReservation> findByOrderCodeAndStatus(
            String orderCode,
            StockReservationStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT r
                FROM StockReservation r
                WHERE r.orderCode = :orderCode
                  AND r.status = :status
            """)
    Optional<StockReservation> findForUpdateByOrderCodeAndStatus(
            String orderCode,
            StockReservationStatus status
    );
}