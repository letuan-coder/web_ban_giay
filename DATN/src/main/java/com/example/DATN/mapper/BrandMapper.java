
package com.example.DATN.mapper;

import com.example.DATN.dtos.request.BrandRequest;
import com.example.DATN.dtos.respone.BrandResponse;
import com.example.DATN.models.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toBrandResponse(Brand brand);

    Brand toBrand(BrandRequest request);

    void updateBrand(@MappingTarget Brand brand, BrandRequest request);
}
