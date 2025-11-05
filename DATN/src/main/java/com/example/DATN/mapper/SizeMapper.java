package com.example.DATN.mapper;

import com.example.DATN.dtos.request.SizeRequest;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.models.Size;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SizeMapper {
    @Mapping(target = "name",source = "name")
    SizeResponse toSizeResponse(Size size);

    Size toSize(SizeRequest request);

    Size toEntity(SizeResponse response);
    void updateSize(@MappingTarget Size size, SizeRequest request);
}
