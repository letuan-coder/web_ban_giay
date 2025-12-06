//package com.example.DATN.models;
//
//import jakarta.persistence.Id;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import org.hibernate.annotations.UuidGenerator;
//import org.springframework.data.elasticsearch.annotations.Document;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Document(indexName = "product_variants")
//public class ProductVariantIndex {
//    @Id
//    @UuidGenerator
//    private UUID id; // dùng ID MySQL hoặc UUID
//
//    private String productId;
//    private String sku;
//    private String name;
//    private String color;
//    private String size;
//
//    private BigDecimal price;
//}
