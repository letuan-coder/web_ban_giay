package com.example.DATN.repositories;

import com.example.DATN.models.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, UUID> {
}