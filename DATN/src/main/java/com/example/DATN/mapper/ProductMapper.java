
package com.example.DATN.mapper;

import com.example.DATN.dtos.request.product.ProductRequest;
import com.example.DATN.dtos.respone.product.ProductResponse;
import com.example.DATN.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImageProductMapper.class, ProductColorMapper.class})
public interface ProductMapper {
    @Mapping(source = "available",target = "available")
    @Mapping(source = "productColors", target = "colorResponses")
    @Mapping(source = "brand.id",target = "brandId")
    @Mapping(source = "category.id",target = "categoryId")
    @Mapping(source = "category.name",target = "categoryName")
    @Mapping(source = "brand.name",target = "brandName")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "brand.id", source = "brandId")
    Product toProduct(ProductRequest productRequest);
}
