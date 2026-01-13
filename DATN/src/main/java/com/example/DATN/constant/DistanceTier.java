package com.example.DATN.constant;

import java.math.BigDecimal;

public enum DistanceTier {

    FREE(0, 3, BigDecimal.ZERO),
    NEAR(3, 7, BigDecimal.valueOf(15)),
    MID(7, 10, BigDecimal.valueOf(20)),
    FAR(10, 20, BigDecimal.valueOf(40)),
    VERY_FAR (20,Integer.MAX_VALUE,BigDecimal.valueOf(50));

    private final double minKm;
    private final double maxKm;
    private final BigDecimal fee;

    DistanceTier(double minKm, double maxKm, BigDecimal fee) {
        this.minKm = minKm;
        this.maxKm = maxKm;
        this.fee = fee;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public static DistanceTier fromDistance(double km) {
        for (DistanceTier tier : values()) {
            if (km >= tier.minKm && km < tier.maxKm) {
                return tier;
            }
        }
        return VERY_FAR;
    }
}
