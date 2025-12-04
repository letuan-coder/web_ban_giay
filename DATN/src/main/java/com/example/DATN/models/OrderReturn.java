package com.example.DATN.models;

import com.example.DATN.constant.OrderReturnStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "order_returns")
public class OrderReturn {
    @Id
    @UuidGenerator
    UUID id;

    String reasonReturn;

    @Enumerated(EnumType.STRING)
    OrderReturnStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id"
            , nullable = false)
    Order order;

    @OneToMany(mappedBy = "orderReturn"
            , cascade = CascadeType.ALL
            , orphanRemoval = true)
    @JsonManagedReference
    List<OrderReturnItem> returnItems;
}
