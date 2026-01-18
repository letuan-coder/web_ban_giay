package com.example.DATN.dtos.respone.voucher;

import com.example.DATN.constant.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    String voucherName;
    String description;
    String voucherCode;
    VoucherType type;
    BigDecimal maxDiscountValue;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    Integer usageLimit;
    LocalDateTime startAt;
    LocalDateTime endAt;
    @Builder.Default
    private Boolean isActive = true;
}
