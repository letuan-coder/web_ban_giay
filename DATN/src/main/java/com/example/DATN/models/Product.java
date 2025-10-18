package com.example.DATN.models;

import com.example.DATN.constant.ProductStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level =  AccessLevel.PRIVATE)
@Table(name = "products")
@Entity
public class Product extends BaseEntity {
    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String slug;

    String description;

    @Column(nullable = false)
    BigDecimal price;

    BigDecimal discountPrice;

    @Column(nullable = false,unique = true)
    String productCode;

    @Enumerated(EnumType.STRING)
    ProductStatus status;

    @Column(nullable = false)
    Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @OneToMany(mappedBy = "product"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    @JsonManagedReference
    private List<ProductVariant> variants;
}