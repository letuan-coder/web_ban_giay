
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
@Table(name = "image_products")
public class ImageProduct extends BaseEntity {
    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "product_color_id")
    @JsonBackReference
    private ProductColor productColor;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "alt_text", length = 500)
    private String altText;
}
