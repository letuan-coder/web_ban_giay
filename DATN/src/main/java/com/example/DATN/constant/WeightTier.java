package com.example.DATN.constant;

public enum WeightTier {
    UP_TO_2KG(2000),
    UP_TO_3KG(3000),
    UP_TO_5KG(5000),
    UP_TO_10KG(1000),
    UP_TO_20KG(2000),
    OVER_20KG(Integer.MAX_VALUE);
    private final int maxKg;
    WeightTier(int maxKg) {
        this.maxKg = maxKg;
    }

    public int getMaxKg() {
        return maxKg;
    }

}
