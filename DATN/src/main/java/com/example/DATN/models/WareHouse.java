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
@Table(name = "warehouses")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WareHouse extends BaseEntity{
    @Id
    @UuidGenerator
    private UUID id;

    private String warehouseCode;

    @Column(nullable = false)
    private String name;

    private String location;

    private String addressDetail;
    private Integer provinceCode;
    private Integer districtCode;
    private Integer wardCode;
    private Integer capacity;
    private Boolean deleted = false;
    private Boolean isCentral;
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL)
    private Set<Stock> stocks = new HashSet<>();

}
