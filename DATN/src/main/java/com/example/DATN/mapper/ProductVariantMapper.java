package com.example.DATN.mapper;

import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.models.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {
    ProductVariant toEntity (ProductVariantResponse response);

    @Mapping(target = "createdAt" ,source = "createdAt")
    @Mapping(target = "id", source = "id")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    List<ProductVariantResponse> toProductVariantResponse(List<ProductVariant> productVariants);

    @Mapping(target = "id", ignore = true)
    ProductVariant toProductVariant(ProductVariantRequest productVariantRequest);
}
