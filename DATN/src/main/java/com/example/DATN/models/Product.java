package com.example.DATN.models;

import com.example.DATN.constant.Is_Available;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
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

    @Column(nullable = false, unique = true)
    String productCode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    Is_Available available=Is_Available.NOT_AVAILABLE;

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
    private List<ProductColor> productColors;
}