package com.example.DATN.mapper;

import com.example.DATN.dtos.request.user.RegisterRequest;
import com.example.DATN.dtos.request.user.UpdateUserRequest;
import com.example.DATN.dtos.respone.user.UserResponse;
import com.example.DATN.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring" ,uses = {RoleMapper.class})
public interface UserMapper {
    @Mapping(target = "roles",ignore = true)
    User Register(RegisterRequest registerRequest);
    @Mapping(target = "roles",ignore = true)
    void updateUser(@MappingTarget User user, UpdateUserRequest request);
    @Mapping(target = "orders",ignore = true)
    @Mapping(target = "roles",source = "roles")
    UserResponse toUserResponse(User user);
}
