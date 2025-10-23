package com.example.DATN.repositories;

import com.example.DATN.models.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, String> {
    boolean existsByHexCode(String hexCode);
    Optional<Color> findByCode(String code);
    Optional<Color> findByName(String colorName);
}