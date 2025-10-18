package com.example.DATN.mapper;

import com.example.DATN.dtos.request.PermissionRequest;
import com.example.DATN.dtos.respone.PermissionResponse;
import com.example.DATN.models.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
