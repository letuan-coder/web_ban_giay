package com.example.DATN.repositories;

import com.example.DATN.constant.BannerType;
import com.example.DATN.models.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, UUID> {
    List<Banner> findByType(BannerType type);
    @Modifying
    @Query("UPDATE Banner b SET b.sortOrder = :sortOrder WHERE b.id = :id")
    void updateSortOrder(@Param("id") UUID id, @Param("sortOrder") Integer sortOrder);
}
