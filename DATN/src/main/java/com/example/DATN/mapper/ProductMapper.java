
package com.example.DATN.mapper;

import com.example.DATN.dtos.request.product.ProductRequest;
import com.example.DATN.dtos.respone.product.ProductDetailReponse;
import com.example.DATN.dtos.respone.product.ProductResponse;
import com.example.DATN.dtos.respone.product.ProductSupplierResponse;
import com.example.DATN.dtos.respone.product.SearchProductResponse;
import com.example.DATN.models.Product;
import com.example.DATN.models.ProductReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        ImageProductMapper.class,
        ProductColorMapper.class,
        ProductVariantMapper.class,
        SupplierMapper.class,
        ProductReviewMapper.class
})
public interface ProductMapper {
    @Mapping(source = "available", target = "available")
    @Mapping(source = "productColors", target = "colorResponses")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.name", target = "brandName")
    @Mapping(source = "thumbnailUrl", target = "ThumbnailUrl")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(product.getReviews()))")
    ProductResponse toProductResponse(Product product);

    default double calculateAverageRating(List<ProductReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToInt(ProductReview::getRating)
                .average()
                .orElse(0.0);
    }

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "price", source = "price")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "category.id", source = "categoryId")
    Product toProduct(ProductRequest productRequest);

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "productCode", source = "productCode")
    @Mapping(target = "id", source = "id")
    ProductSupplierResponse toSupplierDetail(Product product);

    @Mapping(source = "productColors", target = "colorResponses")
    @Mapping(
            source = "productColors",
            target = "variantDetailResponses",
            qualifiedByName = "productColorsToVariantDetails"
    )
    @Mapping(target = "reviewResponses", source = "reviews")
    @Mapping(target = "averageRating", expression = "java(calculateAverageRating(product.getReviews()))")

    ProductDetailReponse toDetail(Product product);

    @Mapping(source = "productColors",
            target = "variantDetailResponses",
            qualifiedByName = "productColorsToVariantDetails")
    SearchProductResponse toSearchDetail(Product product);
}
