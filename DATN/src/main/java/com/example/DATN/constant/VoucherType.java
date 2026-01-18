package com.example.DATN.constant;

import java.math.BigDecimal;

public enum VoucherType {
    PERCENT_DISCOUNT("Giảm"),        // Giảm theo %
    FIXED_AMOUNT("Giảm"),            // Giảm số tiền cố định
    FREE_SHIPPING("Miễn phí vận chuyển"),
    CASHBACK("Hoàn tiền voucher");   // Hoàn tiền

    private final String description;

    VoucherType(String description) {
        this.description = description;
    }

    public String format(BigDecimal value, BigDecimal maxValue) {
        switch (this) {
            case PERCENT_DISCOUNT:
                if (maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0) {
                    return String.format(
                            "%s %s%% giá trị đơn hàng (tối đa %sđ)",
                            description,
                            value.stripTrailingZeros().toPlainString(),
                            maxValue.stripTrailingZeros().toPlainString()
                    );
                }
                return String.format(
                        "%s %s%% giá trị đơn hàng",
                        description,
                        value.stripTrailingZeros().toPlainString()
                );

            case FIXED_AMOUNT:
                return String.format(
                        "%s %sđ giá trị đơn hàng",
                        description,
                        value.stripTrailingZeros().toPlainString()
                );

            case CASHBACK:
                return String.format(
                        "%s %sđ",
                        description,
                        value.stripTrailingZeros().toPlainString()
                );

            case FREE_SHIPPING:
                return description;

            default:
                return description;
        }
    }
}
