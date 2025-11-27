package com.example.DATN.mappers;

import com.example.DATN.dtos.respone.WareHouseResponse;
import com.example.DATN.models.WareHouse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WareHouseMapper {
    WareHouseResponse toWareHouseResponse(WareHouse wareHouse);
}
