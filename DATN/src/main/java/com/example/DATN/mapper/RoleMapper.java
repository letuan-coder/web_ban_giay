package com.example.DATN.mapper;

import com.example.DATN.dtos.request.user.RoleRequest;
import com.example.DATN.dtos.respone.user.RoleResponse;
import com.example.DATN.models.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "permissions",ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
