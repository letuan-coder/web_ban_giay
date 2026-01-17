package com.example.DATN.models;

import com.example.DATN.constant.VoucherClaimStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "voucher_claims", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_voucher_claim_user_voucher",
                columnNames = {"user_id", "voucher_id"}
        )}
)
public class VoucherClaim extends BaseEntity {
    @Id
    @UuidGenerator
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_claim_user")
    )
    User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "voucher_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_voucher_user_voucher"))
    @JsonBackReference
    private Voucher voucher;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherClaimStatus status;

    @Column(name = "max_usage", nullable = false)
    private Integer maxUsage;

    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount=0;

    @Column(name = "source_order_id")
    UUID sourceOrderId;

}
