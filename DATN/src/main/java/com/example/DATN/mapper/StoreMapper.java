package com.example.DATN.mapper;

import com.example.DATN.dtos.request.StoreRequest;
import com.example.DATN.dtos.respone.StoreResponse;
import com.example.DATN.models.Store;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    Store toStore(StoreRequest request);
    StoreResponse toStoreResponse(Store store);
    Store toEntity (StoreResponse responses);
    void updateStore(@MappingTarget Store store, StoreRequest request);
}
