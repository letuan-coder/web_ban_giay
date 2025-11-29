package com.example.DATN.mapper;

import com.example.DATN.dtos.request.product.ProductColorRequest;
import com.example.DATN.dtos.respone.product.ProductColorDetailResponse;
import com.example.DATN.dtos.respone.product.ProductColorResponse;
import com.example.DATN.models.ProductColor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, ColorMapper.class, ProductVariantMapper.class})
public interface ProductColorMapper {
    ProductColor toEntity(ProductColorResponse response);

    @Mapping(target = "variantResponses", source = "variants")
    @Mapping(target = "colorName",source = "color.name")
    @Mapping(target = "colorCode",source = "color.code")
    @Mapping(target = "colorHexCode",source = "color.hexCode")
    ProductColorResponse toProductColorResponse(ProductColor productColor);

    List<ProductColorResponse> toProductColorResponses(List<ProductColor> productColors);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "color", ignore = true)
    ProductColor toProductColor(ProductColorRequest request);

    @Mapping(source = "id", target = "productColorId")
    @Mapping(source = "color.name", target = "colorName")
    @Mapping(source = "color.hexCode", target = "hexCode")
    ProductColorDetailResponse toDetail(ProductColor response);
}
