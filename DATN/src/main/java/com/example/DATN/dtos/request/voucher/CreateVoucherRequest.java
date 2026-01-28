package com.example.DATN.dtos.request.voucher;

import com.example.DATN.constant.VoucherApply;
import com.example.DATN.constant.VoucherTarget;
import com.example.DATN.constant.VoucherType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateVoucherRequest {
    private VoucherType type;
    private VoucherApply apply;
    private VoucherTarget target;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Integer usageLimit;
    private BigDecimal maxDiscountValue;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @Builder.Default
    private Boolean isActive = true;
}
