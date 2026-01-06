package com.example.DATN.models;

import com.example.DATN.constant.VoucherApply;
import com.example.DATN.constant.VoucherTarget;
import com.example.DATN.constant.VoucherType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vouchers")
public class Voucher {
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    UUID id;
    @Column(name = "voucher_code", nullable = false, unique = true)
    String voucherCode;
    @Column(name = "voucher_name", nullable = false, unique = true)
    String voucherName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VoucherType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VoucherTarget target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VoucherApply apply;
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
