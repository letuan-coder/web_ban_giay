package com.example.DATN.models;

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
@Table(
        name = "voucher_usages",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_voucher_usage_order",
                        columnNames = {"order_id"}
                )
        },
        indexes = {
                @Index(name = "idx_voucher_usage_claim", columnList = "voucher_claim_id")
        }
)
public class VoucherUsage {
    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "voucher_claim_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_usage_claim")
    )
    private VoucherClaim voucherClaim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_usage_order")
    )
    private Order order;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
}
