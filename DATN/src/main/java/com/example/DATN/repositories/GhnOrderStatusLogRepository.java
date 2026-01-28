package com.example.DATN.repositories;

import com.example.DATN.models.GhnOrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GhnOrderStatusLogRepository extends JpaRepository<GhnOrderStatusLog, UUID> {

    boolean existsByOrder_IdAndStatusAndGhnUpdatedAt(UUID orderId, String status, LocalDateTime ghnUpdatedAt);}