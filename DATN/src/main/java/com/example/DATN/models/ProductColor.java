package com.example.DATN.models;

import com.example.DATN.constant.Is_Available;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "product_colors")
@Entity
public class ProductColor extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id; // ví dụ COL08

    @ManyToOne
    @JoinColumn(name = "color_code")
    Color color;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference
            @OnDelete(action = OnDeleteAction.CASCADE)
    Product product;

    @OneToMany(mappedBy = "productColor"
            , fetch = FetchType.LAZY
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    @JsonManagedReference
    List<ImageProduct> images = new ArrayList<>();

    @OneToMany(mappedBy = "productColor"
            ,fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference
    private List<ProductVariant> variants;// Danh sách size + stock

    @Enumerated(EnumType.STRING)
    @Builder.Default
    Is_Available isAvailable=Is_Available.AVAILABLE;

}
