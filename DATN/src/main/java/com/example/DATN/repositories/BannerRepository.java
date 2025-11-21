package com.example.DATN.repositories;

import com.example.DATN.constant.BannerType;
import com.example.DATN.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {
    List<Banner> findByType(BannerType type);
}
