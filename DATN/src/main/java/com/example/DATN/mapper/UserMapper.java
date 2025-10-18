package com.example.DATN.mapper;

import com.example.DATN.dtos.request.RegisterRequest;
import com.example.DATN.dtos.request.UpdateUserRequest;
import com.example.DATN.dtos.respone.UserResponse;
import com.example.DATN.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles",ignore = true)
    User Register(RegisterRequest registerRequest);
    @Mapping(target = "roles",ignore = true)
    void updateUser(@MappingTarget User user, UpdateUserRequest request);
    @Mapping(target = "orders",ignore = true)
    UserResponse toUserResponse(User user);
}
