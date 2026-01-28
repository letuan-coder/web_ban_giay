package com.example.DATN.specification;

import com.example.DATN.constant.ProductStatus;
import com.example.DATN.models.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(
            String productName, Double priceMin, Double priceMax, ProductStatus status,Long brandId,Long categoryId,String colorCode,String sizeCode) {
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
            if (colorCode != null && !colorCode.isEmpty()) {
                Join<Product, ProductColor> productColorJoin = root.join("productColors", JoinType.INNER);
                Join<ProductColor, Color> colorJoin = productColorJoin.join("color", JoinType.INNER);

                predicates.add(criteriaBuilder.equal(colorJoin.get("code"), colorCode));
            }

            if (sizeCode != null && !sizeCode.isEmpty()) {
                Join<Product, ProductColor> pcJoin = root.join("productColors", JoinType.INNER);
                Join<ProductColor, ProductVariant> variantJoin = pcJoin.join("variants", JoinType.INNER);
                Join<ProductVariant, Size> sizeJoin = variantJoin.join("size", JoinType.INNER);

                predicates.add(criteriaBuilder.equal(sizeJoin.get("code"), sizeCode));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
