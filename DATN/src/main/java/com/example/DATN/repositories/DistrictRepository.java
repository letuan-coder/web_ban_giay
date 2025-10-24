package com.example.DATN.repositories;

import com.example.DATN.models.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {
    boolean existsByCode(String id);

    List<District> findByParentCode(String parentCode);
}
