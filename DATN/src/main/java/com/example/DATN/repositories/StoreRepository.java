package com.example.DATN.repositories;

import com.example.DATN.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
   Optional<Store> findByCode(String code);
   List<Store> findAllByWardCode(Integer wardCode);
   List<Store> findAllByProvinceCode(Integer provinceCode);
   List<Store> findAllByDistrictCode(Integer DistrictCode);

}
