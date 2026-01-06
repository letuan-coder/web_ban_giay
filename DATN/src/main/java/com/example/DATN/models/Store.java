package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "store")
public class Store extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;
    private String code;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String location;

    private Double Longitude;
    private Double Latitude;

    private Integer provinceCode;
    private Integer districtCode;
    private Integer wardCode;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    private Boolean active = true;

    @Column(nullable = false, unique = true)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private Set<Stock> stocks = new HashSet<>();

}
