package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.ProvinceResponse;
import com.example.DATN.models.Province;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProvinceMapper {
    ProvinceMapper INSTANCE = Mappers.getMapper(ProvinceMapper.class);

    ProvinceResponse toProvinceResponse(Province province);
}
