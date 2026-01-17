package com.example.DATN.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, UUID> {
}