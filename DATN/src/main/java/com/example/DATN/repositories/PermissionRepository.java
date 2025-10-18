package com.example.DATN.repositories;

import com.example.DATN.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission,String> {
}
