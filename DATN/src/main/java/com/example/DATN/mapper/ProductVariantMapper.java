package com.example.DATN.mapper;

import com.example.DATN.dtos.request.ProductVariantRequest;
import com.example.DATN.dtos.respone.ProductVariantResponse;
import com.example.DATN.models.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductVariant toProductVariant(ProductVariantRequest productVariantRequest);
}
