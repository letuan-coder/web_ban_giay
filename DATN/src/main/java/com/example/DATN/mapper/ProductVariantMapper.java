package com.example.DATN.mapper;

import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.respone.product.ProductVariantDetailResponse;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",uses = {StockMapper.class})
public interface ProductVariantMapper {
    ProductVariant toEntity (ProductVariantResponse response);

    @Mapping(target = "createdAt" ,source = "createdAt")
    @Mapping(target = "id", source = "id")
    ProductVariantResponse toProductVariantResponse(ProductVariant productVariant);

    List<ProductVariantResponse> toProductVariantResponse(List<ProductVariant> productVariants);

    @Mapping(target = "id", ignore = true)
    ProductVariant toProductVariant(ProductVariantRequest productVariantRequest);


    @Mapping(source = "size.name", target = "size")
    @Mapping(source = "productColor.color.name",target = "colorName")
    @Mapping(source = "productColor.color.hexCode",target = "colorHex")
    @Mapping(source = "stocks",target = "stocks")
    ProductVariantDetailResponse basicToDetail(ProductVariant variant);

    @Mapping(target = "size.name", source = "size")
    @Mapping(target = "productColor.color.name",source = "colorName")
    @Mapping(target = "productColor.color.hexCode",source = "colorHex")
    @Mapping(target = "stocks",source = "stocks")
    ProductVariant DetailToVariantMapper(ProductVariantDetailResponse variant);

    @Named("toVariantDetail")
    default ProductVariantDetailResponse toDetail (ProductVariant variant) {
        if (variant == null) {
            return null;
        }
        ProductVariantDetailResponse response = basicToDetail(variant);
        if (response != null) {
            response.calculateAndSetTotalStock();
        }
        return response;
    }

    @IterableMapping(qualifiedByName = "toVariantDetail")
    List<ProductVariantDetailResponse> mapVariantsToDetails(List<ProductVariant> variants);
    @Named("productColorsToVariantDetails")
    default List<ProductVariantDetailResponse> productColorsToVariantDetails(List<ProductColor> productColors) {
        if (productColors == null) {
            return Collections.emptyList();
        }

        List<ProductVariant> allVariants = productColors.stream()
                .flatMap(pc -> pc.getVariants().stream())
                .collect(Collectors.toList());

        return mapVariantsToDetails(allVariants);
    }

}
