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
@Table(name = "color")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Color extends BaseEntity {
    @Id
    @Column(length = 10)
    String code; // ví dụ COL08

    @Column(nullable = false)
    String name; // tên màu, ví dụ "White"


    @Column(unique = true)
    String hexCode;

    @OneToMany(mappedBy = "color"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
            @JsonBackReference
    List<ProductVariant> variants = new ArrayList<>();


}
