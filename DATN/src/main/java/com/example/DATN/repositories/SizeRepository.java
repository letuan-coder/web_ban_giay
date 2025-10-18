package com.example.DATN.repositories;

import com.example.DATN.models.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
  Optional<Size> findByCode(String code);
  Optional<Size> findByName(String Name);
  boolean existsByName (String name);
  }