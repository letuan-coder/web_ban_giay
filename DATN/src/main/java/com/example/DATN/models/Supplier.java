package com.example.DATN.models;

import com.example.DATN.constant.SupplierStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="suppliers")
//nhà cung ứng
public class Supplier {
    @Id
    @UuidGenerator
    private UUID id;

    private String supplierCode;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String taxCode; // mã số thuế

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @OneToMany(mappedBy = "supplier",
            cascade = CascadeType.PERSIST,
            orphanRemoval = true)
    private Set<Product> product;

    @Column(columnDefinition = "TEXT")
    private String SupplierAddress;

    private SupplierStatus status;
}
