package com.example.DATN.repositories;

import com.example.DATN.models.ForgotToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotTokenRepository extends JpaRepository<ForgotToken, Integer> {
   Optional<ForgotToken> findByToken(String token);
   Optional<ForgotToken>findByEmail(String email);
}