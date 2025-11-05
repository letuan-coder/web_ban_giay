package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.georaphy.DistrictResponse;
import com.example.DATN.models.District;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ProvinceMapper.class})
public interface DistrictMapper {
    DistrictMapper INSTANCE = Mappers.getMapper(DistrictMapper.class);

    @Mapping(source = "province", target = "province")
    DistrictResponse toDistrictResponse(District district);
}
