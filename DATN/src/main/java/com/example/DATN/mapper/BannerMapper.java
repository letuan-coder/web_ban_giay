package com.example.DATN.mapper;

import com.example.DATN.dtos.request.BannerRequest;
import com.example.DATN.dtos.respone.BannerResponse;
import com.example.DATN.models.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    Banner toBanner(BannerRequest request);

    @Mapping(source = "sortOrder",target = "sortOrder")
    @Mapping(source = "imageUrl",target = "imageUrl")
    @Mapping(source = "type",target = "type")
    BannerResponse toBannerResponse(Banner banner);

    @Mapping(source = "sortOrder",target = "sortOrder")
    @Mapping(source = "imageUrl",target = "imageUrl")
    @Mapping(source = "type",target = "type")
    void updateBanner(@MappingTarget Banner banner, BannerRequest request);
}
