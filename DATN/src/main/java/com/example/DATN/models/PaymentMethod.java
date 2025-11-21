package com.example.DATN.models;

import com.example.DATN.constant.Is_Available;
import com.example.DATN.constant.PaymentMethodEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Setter
@Getter
@Builder
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
    String displayName;

    @Enumerated(EnumType.STRING)
    PaymentMethodEnum Status;

    @OneToMany(mappedBy = "paymentMethod", fetch = FetchType.LAZY)
    List<Order> orders;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    Is_Available isAvailable=Is_Available.AVAILABLE;
}

