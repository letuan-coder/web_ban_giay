package com.example.DATN.mapper;

import com.example.DATN.dtos.request.ColorRequest;
import com.example.DATN.dtos.respone.ColorResponse;
import com.example.DATN.models.Color;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ColorMapper {
    ColorResponse toColorResponse(Color color);

    Color toColor(ColorRequest request);
    Color toEntity (ColorResponse response);
    void updateColor(@MappingTarget Color color, ColorRequest request);
}
