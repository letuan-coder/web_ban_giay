
package com.example.DATN.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "image_products")
public class ImageProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "alt_text", length = 500)
    private String altText;
}
