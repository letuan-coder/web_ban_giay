package com.example.DATN.mapper;

import com.example.DATN.dtos.request.ColorRequest;
import com.example.DATN.dtos.respone.ColorResponse;
import com.example.DATN.models.Color;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ColorMapper {
    Color toEntity(ColorResponse response);
    ColorResponse toColorResponse(Color color);
    void updateColor(@MappingTarget Color color, ColorRequest request);
}
