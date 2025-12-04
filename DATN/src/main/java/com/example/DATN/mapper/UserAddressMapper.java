package com.example.DATN.mapper;

import com.example.DATN.dtos.request.user_address.UserAddressRequest;
import com.example.DATN.dtos.respone.user_address.UserAddressResponse;
import com.example.DATN.models.UserAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {
    UserAddressResponse toResponse(UserAddress userAddress);

    @Mapping(target = "provinceCode", ignore = true)
    @Mapping(target = "districtCode", ignore = true)
    @Mapping(target = "wardCode", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateUserAddress(@MappingTarget UserAddress userAddress, UserAddressRequest request);
}
