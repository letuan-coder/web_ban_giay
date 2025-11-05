package com.example.DATN.services;

import com.example.DATN.dtos.request.user.PermissionRequest;
import com.example.DATN.dtos.respone.user.PermissionResponse;
import com.example.DATN.mapper.PermissionMapper;
import com.example.DATN.models.Permission;
import com.example.DATN.repositories.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse Create(PermissionRequest request){
        Permission permission= permissionMapper.toPermission(request);
        permission= permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> GetAllPermission(){
        var permission = permissionRepository.findAll();
        return permission.stream().map(permissionMapper::toPermissionResponse).toList();
    }

   public void DeletePermission (String name){
        permissionRepository.deleteById(name);
   }
}
