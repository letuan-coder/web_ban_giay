package com.example.DATN.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "image_orders_return")
public class ImageOrderReturn {
    @Id
    Long id;

    @ManyToOne
    @JsonBackReference
    OrderReturnItem orderReturnItem;

    String imageUrl;
}
