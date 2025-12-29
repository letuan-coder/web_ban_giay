package com.example.DATN.models;

import com.example.DATN.constant.Is_Available;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variant", uniqueConstraints = @UniqueConstraint(
        name = "uk_product_color_size",
        columnNames = {"product_color_id", "size_code"}
))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant extends BaseEntity {
    @Id
    @UuidGenerator
    UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_color_id", nullable = false)
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    ProductColor productColor;

    @ManyToOne
    @JoinColumn(name = "size_code")
    Size size;

    BigDecimal price;


    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL)
    private Set<Stock> stocks = new HashSet<>();

    //for shipping
    Integer weight;
    Integer height;
    Integer width;
    Integer length;

    @Column(nullable = false, unique = true)
    String sku;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    Is_Available isAvailable = Is_Available.AVAILABLE;
}