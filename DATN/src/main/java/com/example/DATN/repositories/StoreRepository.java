package com.example.DATN.repositories;

import com.example.DATN.models.Store;
import com.example.DATN.repositories.projection.NearestStoreProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByCode(String code);

    List<Store> findAllByWardCode(Integer wardCode);

    List<Store> findAllByProvinceCode(Integer provinceCode);

    List<Store> findAllByDistrictCode(Integer DistrictCode);

    @Query(value = """
    SELECT 
        BIN_TO_UUID(s.id) AS id,
        s.name            AS name,
        s.latitude        AS latitude,
        s.longitude       AS longitude,
        (
            6371 * acos(
                cos(radians(:lat))
                * cos(radians(s.latitude))
                * cos(radians(s.longitude) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(s.latitude))
            )
        ) AS distanceKm
    FROM store s
    JOIN stock st ON st.store_id = s.id
    JOIN product_variant pv ON pv.id = st.variant_id
    WHERE pv.sku IN (:skus)
      AND st.quantity > 0
      AND s.active = 1
      AND s.latitude  BETWEEN :lat - 0.2 AND :lat + 0.2
      AND s.longitude BETWEEN :lng - 0.2 AND :lng + 0.2
    GROUP BY 
        s.id, s.name, s.latitude, s.longitude
    HAVING COUNT(DISTINCT pv.sku) = :totalSku
    ORDER BY distanceKm
    LIMIT 1
""", nativeQuery = true)
    Optional<NearestStoreProjection> findNearestStoreWithAllSku(
            @Param("skus") List<String> skus,
            @Param("totalSku") Integer totalSku,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );

    @Query(value = """
                SELECT
                    s.id AS id,
                    s.name AS name,
                    ST_Distance_Sphere(
                        POINT(s.longitude, s.latitude),
                        POINT(:lon, :lat)
                    ) / 1000 AS distance_km
                FROM store s
                WHERE s.available = 'AVAILABLE'
                ORDER BY distance_km
                LIMIT 1
            """, nativeQuery = true)
    Optional<NearestStoreProjection> findNearestStore(
            @Param("lat") Double latitude,
            @Param("lon") Double longitude
    );

}
