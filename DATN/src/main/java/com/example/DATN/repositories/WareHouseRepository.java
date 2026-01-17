package com.example.DATN.repositories;

import com.example.DATN.models.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WareHouseRepository extends JpaRepository<WareHouse, UUID> {
    Optional<WareHouse> findBywarehouseCode(String warehouseCode);
    List<WareHouse> findAllByDeletedFalse();
    List<WareHouse> findAllByDeletedFalseAndIsCentralTrue();
}
