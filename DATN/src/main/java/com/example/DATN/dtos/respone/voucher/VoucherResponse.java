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
    String voucherCode;
    VoucherType type;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Builder.Default
    private Boolean isActive = true;
}
