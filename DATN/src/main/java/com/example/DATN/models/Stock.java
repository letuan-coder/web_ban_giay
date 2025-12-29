package com.example.DATN.models;

import com.example.DATN.constant.StockType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "stock")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock extends BaseEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Enumerated(EnumType.STRING)
    private StockType stockType;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private WareHouse warehouse;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    private Integer minQuantity;
    private Integer quantity = 0;
}

