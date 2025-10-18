package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "payment_methods")
public class PaymentMethod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 255)
     String name;

    @Column(length = 1000)
     String description;

    @OneToMany(mappedBy = "paymentMethod", fetch = FetchType.LAZY)
    List<Order> orders;
}

