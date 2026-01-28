package com.example.DATN.repositories;

import com.example.DATN.models.ImageOrderReturn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ImageOrderReturnRepository extends JpaRepository<ImageOrderReturn, UUID> {
}