package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store")
public class Store extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String location;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private Boolean active = true;

    @Column(nullable = false, unique = true)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private Set<Stock> stocks = new HashSet<>();


}
