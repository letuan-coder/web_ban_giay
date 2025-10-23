package com.example.DATN.mapper;

import com.example.DATN.dtos.request.ProductColorRequest;
import com.example.DATN.dtos.respone.ProductColorResponse;
import com.example.DATN.dtos.respone.ProductResponse;
import com.example.DATN.models.ProductColor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, ColorMapper.class, ProductVariantMapper.class})
public interface ProductColorMapper {
    ProductColor toEntity(ProductResponse response);

    @Mapping(target = "variantResponses", source = "variants")
    ProductColorResponse toProductColorResponse(ProductColor productColor);

    List<ProductColorResponse> toProductColorResponses(List<ProductColor> productColors);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "color", ignore = true)
    ProductColor toProductColor(ProductColorRequest request);
}
