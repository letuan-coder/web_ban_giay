package com.example.DATN.repositories;

import com.example.DATN.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByName(String name);
    Optional<Supplier> findByTaxCode(String taxCode);
    Optional<Supplier> findByEmail(String email);
    Optional<Supplier> findBySupplierCode(String code);
    Optional<Supplier> findByPhoneNumber(String phoneNumber);
}
