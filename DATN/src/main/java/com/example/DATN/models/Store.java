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
    @Column(nullable = false, unique = true)
    private String code;
    @Column(name = "store_code_ghn",nullable = false, unique = true)
    private Integer storeCodeGHN;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String location;

    private Double Longitude;
    private Double Latitude;

    @Column(name = "province_code",nullable = false, unique = true)

    private Integer provinceCode;
    @Column(name = "district_code",nullable = false, unique = true)

    private Integer districtCode;
    @Column(name = "ward_code",nullable = false, unique = true)

    private String wardCode;
    @Column(name = "ward_name",nullable = false, unique = true)

    private String wardName;
    @Column(name = "district_name",nullable = false, unique = true)

    private String districtName;
    @Column(name = "province_name",nullable = false, unique = true)

    private String provinceName;
    @Column(name = "phone_number",nullable = false, unique = true)

    private String phoneNumber;

    private Boolean active = true;

    @Column(nullable = false, unique = true)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private Set<Stock> stocks = new HashSet<>();

}
