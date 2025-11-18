package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "brands")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Brand extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
    String name;

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY
            , cascade = CascadeType.ALL, orphanRemoval = true)
    List<Product> products;

}
