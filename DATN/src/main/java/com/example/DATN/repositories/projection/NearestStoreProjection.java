package com.example.DATN.repositories.projection;

import java.util.UUID;

public interface NearestStoreProjection {
    UUID getId();
    String getName();
    Double getDistanceKm();
}
