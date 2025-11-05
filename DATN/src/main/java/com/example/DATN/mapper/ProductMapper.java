
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
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toProduct(ProductRequest productRequest);
}
