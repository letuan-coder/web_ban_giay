package com.example.DATN.mapper;

import com.example.DATN.dtos.request.SizeRequest;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.models.Size;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SizeMapper {
    SizeResponse toSizeResponse(Size size);

    Size toSize(SizeRequest request);

    void updateSize(@MappingTarget Size size, SizeRequest request);
}
