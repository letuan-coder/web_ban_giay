package com.example.DATN.constant;

public enum WeightTier {
    UP_TO_1KG(1),
    UP_TO_3KG(3),
    UP_TO_5KG(5),
    UP_TO_10KG(10),
    UP_TO_20KG(20),
    OVER_20KG(Integer.MAX_VALUE);
    private final int maxKg;
    WeightTier(int maxKg) {
        this.maxKg = maxKg;
    }

    public int getMaxKg() {
        return maxKg;
    }

}
