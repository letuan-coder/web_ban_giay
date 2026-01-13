package com.example.DATN.constant;

import java.math.BigDecimal;

public enum VoucherType {
    PERCENT_DISCOUNT("Giảm"),   // Giảm theo %
    FIXED_AMOUNT("Giảm"),       // Giảm số tiền cố định
    FREE_SHIPPING("Miễn phí vận chuyển"),      // Miễn phí vận chuyển
    CASHBACK("Hoàn tiền voucher");   // Hoàn tiền

    private final String description;

    VoucherType(String description) {
        this.description = description;
    }

    public String format(BigDecimal value) {
        switch (this) {
            case PERCENT_DISCOUNT:
                return description + " " + value + "% giá trị đơn hàng";
            case FIXED_AMOUNT:
                return description + " " + value + "đ giá trị đơn hàng";
            case CASHBACK:
                return description + " " + value + "đ";
            default:
                return description;
        }
    }
}
