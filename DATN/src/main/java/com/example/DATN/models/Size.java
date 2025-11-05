package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "size")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Size extends BaseEntity {
    @Id
    @Column(length = 10)
    String code;

    @Column(nullable = false)
    Integer name;


    @OneToMany(mappedBy = "size",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonBackReference
    List<ProductVariant> variants = new ArrayList<>();
}
