package com.example.DATN.models;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product_variants")
public class ProductVariantIndex {
    @Id
    @UuidGenerator
    private UUID id;
    @Field(type = FieldType.Text)
    private String productId;
    @Field(type = FieldType.Text)
    private String productCode;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String thumbnailUrl;
    private BigDecimal price;
}
