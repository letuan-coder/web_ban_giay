package com.example.DATN.specification;

import com.example.DATN.constant.ProductStatus;
import com.example.DATN.models.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(
            String productName, Double priceMin, Double priceMax, ProductStatus status,Long brandId,Long categoryId,Integer size,String color) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productName != null && !productName.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + productName.toLowerCase() + "%"));
            }

            if (priceMin != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), new BigDecimal(priceMin)));
            }

            if (priceMax != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), new BigDecimal(priceMax)));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("available"), status));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId!=null){
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }

            // Correctly join for size filtering
            if (size != null) {
                Join<Product, ProductColor> productColorJoin = root.join("productColors");
                Join<ProductColor, ProductVariant> productVariantJoin = productColorJoin.join("variants");
                Join<ProductVariant, Size> sizeJoin = productVariantJoin.join("size");
                predicates.add(criteriaBuilder.equal(sizeJoin.get("name"), size.toString()));
            }

            // Correctly join for color filtering
            if (color != null && !color.isEmpty()) {
                Join<Product, ProductColor> productColorJoin = root.join("productColors");
                Join<ProductColor, Color> colorJoin = productColorJoin.join("color");
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(colorJoin.get("name")), "%" + color.toLowerCase() + "%"));
            }

            // Add distinct to avoid duplicate products from joins
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
