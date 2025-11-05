package com.example.DATN.mapper;

import com.example.DATN.dtos.request.user.PermissionRequest;
import com.example.DATN.dtos.respone.user.PermissionResponse;
import com.example.DATN.models.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
