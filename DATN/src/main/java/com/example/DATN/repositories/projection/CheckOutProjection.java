package com.example.DATN.repositories.projection;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckOutProjection {
    private boolean isStore;
    private UUID storeId;
    private Double distanceKm;
    public static CheckOutProjection store(UUID storeId, Double distanceKm) {
        return new CheckOutProjection(true, storeId, distanceKm);
    }

    public static CheckOutProjection ghn() {
        return new CheckOutProjection(false, null, null);
    }

}
