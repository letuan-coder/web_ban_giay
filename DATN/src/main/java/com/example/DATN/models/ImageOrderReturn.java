package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "image_orders_return")
public class ImageOrderReturn {
    @Id
    @UuidGenerator
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_return_item_id", nullable = false)
    @JsonBackReference
    private OrderReturnItem orderReturnItem;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;
}
