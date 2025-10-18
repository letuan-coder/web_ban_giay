package com.example.DATN.repositories;

import com.example.DATN.models.InvalidateToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidateTokenRepository extends JpaRepository<InvalidateToken,String> {
}
