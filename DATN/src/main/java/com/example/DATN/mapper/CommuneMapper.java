package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.georaphy.CommuneResponse;
import com.example.DATN.models.Commune;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {DistrictMapper.class})
public interface CommuneMapper {
    CommuneMapper INSTANCE = Mappers.getMapper(CommuneMapper.class);


    CommuneResponse toCommuneResponse(Commune commune);
}
