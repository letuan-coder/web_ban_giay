package com.example.DATN.models;

import com.example.DATN.constant.VoucherApply;
import com.example.DATN.constant.VoucherTarget;
import com.example.DATN.constant.VoucherType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @Column(name = "voucher_name", nullable = false)
    String voucherName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VoucherType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    VoucherTarget target;

    @Column(name = "description",length = 255)
    private String description;

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
    @Builder.Default
    private int usedCount = 0;

    @Column(name = "created_by")
    private String createdBy;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<VoucherClaim> voucherUsers = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive = true;
}
