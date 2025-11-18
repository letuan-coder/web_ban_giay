package com.example.DATN.models;

import com.example.DATN.constant.Is_Available;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_color_id", nullable = false)
    @JsonBackReference
    @OnDelete(action = OnDeleteAction.CASCADE)
    ProductColor productColor;

    @ManyToOne
    @JoinColumn(name = "size_code")
    @JsonManagedReference
    Size size;

    BigDecimal price;

    BigDecimal discountPrice;

    Integer stock;

    @Column(nullable = false, unique = true)
    String sku;

    @ManyToOne
    @JsonBackReference
    CartItem cartItem;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    Is_Available isAvailable = Is_Available.AVAILABLE;

    @ManyToMany(mappedBy = "productVariants")
    private Set<Promotion> promotions = new HashSet<>();
}