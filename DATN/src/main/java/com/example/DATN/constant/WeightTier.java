package com.example.DATN.constant;

import java.math.BigDecimal;

public enum WeightTier {
    FREE_WEIGHT(0, 3000, BigDecimal.ZERO),
    LIGHT(3000, 5000, BigDecimal.valueOf(5000)),
    MEDIUM(5000, 10000, BigDecimal.valueOf(15000)),
    HEAVY(10000,150000,BigDecimal.valueOf(20000)),
    VERY_HEAVY(150000, Integer.MAX_VALUE, BigDecimal.valueOf(40000));

    private final int minGram;
    private final int maxGram;
    private final BigDecimal extraFee;

    WeightTier(int minGram, int maxGram, BigDecimal extraFee) {
        this.minGram = minGram;
        this.maxGram = maxGram;
        this.extraFee = extraFee;
    }

    public BigDecimal getExtraFee() {
        return extraFee;
    }

    public static WeightTier fromWeight(int totalGram) {
        for (WeightTier tier : values()) {
            if (totalGram > tier.minGram && totalGram <= tier.maxGram) {
                return tier;
            }
        }
        return VERY_HEAVY;
    }
}
