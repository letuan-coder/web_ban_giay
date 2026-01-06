package com.example.DATN.constant;

public enum VoucherType {
    PERCENT_DISCOUNT("PD"),   // Giảm theo %
    FIXED_AMOUNT("FA"),       // Giảm số tiền cố định
    FREE_SHIPPING("FS"),      // Miễn phí vận chuyển
    CASHBACK("CB");   // Hoàn tiền
    private final String prefix;

    VoucherType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
