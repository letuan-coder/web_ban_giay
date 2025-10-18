package com.example.DATN.constant.Util;

public class ProductCodeUtil {
    public static String generateProductCode(String productCode) {
        if (productCode == null) {
            throw new IllegalArgumentException("ProductCode");
        }
        return String.format("%s",
                productCode.toUpperCase());
    }
}
