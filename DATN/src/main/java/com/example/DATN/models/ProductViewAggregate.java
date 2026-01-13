package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "product_view_aggregate",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"variant_id", "time_bucket"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductViewAggregate {
    @Id
    @UuidGenerator
    private UUID id;
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    @Column(name = "variant_id", nullable = false)
    private UUID variantId ;

    @Column(name = "time_bucket", nullable = false)
    private Instant timeBucket = Instant.now();


    @Column(name = "total_view", nullable = false)
    @Builder.Default
    private Integer totalView = 0;
}
