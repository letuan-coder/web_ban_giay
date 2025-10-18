package com.example.DATN.repositories;

import com.example.DATN.models.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, String> {
    boolean existsByCode(String code);
}
