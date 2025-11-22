package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "warehouses")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WareHouse extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    private Integer capacity;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean deleted = false;


    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private Set<Stock> stocks = new HashSet<>();

}
