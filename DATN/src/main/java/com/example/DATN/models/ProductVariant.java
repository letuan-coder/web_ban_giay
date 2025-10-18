package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_variant")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    Product product;

    @ManyToOne
    @JoinColumn(name = "size_code")
    @JsonManagedReference
    Size size;

    @ManyToOne
    @JoinColumn(name = "color_code")
    @JsonManagedReference
    Color color;

    BigDecimal price;
    Integer stock;

    @Column(nullable = false, unique = true)
    String sku;
    @OneToMany(mappedBy = "productVariant"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    @JsonManagedReference
    List<ImageProduct> images;
}