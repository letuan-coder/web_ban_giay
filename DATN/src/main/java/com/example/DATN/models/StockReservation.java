package com.example.DATN.models;

import com.example.DATN.constant.StockReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "stock_reservations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_order_stock",
                        columnNames = {"order_code", "stock_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_status_expires",
                        columnList = "status, expires_at"
                )
        }
)
public class StockReservation extends BaseEntity{
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "order_code", nullable = false, length = 64)
    private String orderCode;

    @Column(name = "stock_id", nullable = false)
    private UUID stockId;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockReservationStatus status;


    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
