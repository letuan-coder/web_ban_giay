package com.example.DATN.models;

import com.example.DATN.constant.VariantType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "product_views", indexes =
        {@Index(name = "idx_variant_time", columnList = "variant_id, created_at"),
                @Index(name = "idx_user_time", columnList = "user_id, created_at"),
                @Index(name = "idx_anon_time", columnList = "anonymous_id, created_at")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductView extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "variant_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VariantType variantType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "anonymous_id", length = 64)
    private String anonymousId;
}
