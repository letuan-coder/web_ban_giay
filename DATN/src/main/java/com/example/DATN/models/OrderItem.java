package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {
    @Id
    @UuidGenerator
    UUID id;
    String name;
    String code;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("order_items")
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productVariant_id", nullable = false)
    ProductVariant productVariant;

    @Column(nullable = false)
    Integer quantity;

    private Integer weight;
    private Integer height;
    private Integer width;
    private Integer length;

    @Column(nullable = false)
    private BigDecimal price;
    private Boolean Rated;

    private LocalDate returnDate;
}

