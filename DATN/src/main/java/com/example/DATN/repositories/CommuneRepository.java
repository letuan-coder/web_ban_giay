package com.example.DATN.repositories;

import com.example.DATN.models.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, String> {
    List<Commune> findByParentCode(String parentCode);
}
