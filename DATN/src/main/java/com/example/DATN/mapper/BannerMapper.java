package com.example.DATN.mapper;

import com.example.DATN.dtos.request.BannerRequest;
import com.example.DATN.dtos.respone.BannerResponse;
import com.example.DATN.models.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    Banner toBanner(BannerRequest request);

    BannerResponse toBannerResponse(Banner banner);

    void updateBanner(@MappingTarget Banner banner, BannerRequest request);
}
