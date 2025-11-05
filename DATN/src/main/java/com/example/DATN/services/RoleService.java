package com.example.DATN.services;

import com.example.DATN.dtos.request.user.RoleRequest;
import com.example.DATN.dtos.respone.user.RoleResponse;
import com.example.DATN.mapper.RoleMapper;
import com.example.DATN.models.Role;
import com.example.DATN.repositories.PermissionRepository;
import com.example.DATN.repositories.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    private final PermissionRepository permissionRepository;
    RoleMapper roleMapper;
    RoleRepository roleRepository;

    public List<RoleResponse> getAllRoles() {

        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public Optional<Role> getRoleById(String id) {
        return roleRepository.findById(id);
    }

    public RoleResponse createRole(RoleRequest request) {
        Role roles = roleMapper.toRole(request);
        var permission = permissionRepository.findAllById(request.getPermission());
        roles.setPermissions(new HashSet<>(permission));
        roleRepository.save(roles);
        return  roleMapper.toRoleResponse(roles);
    }

    public void deleteRole(String id) {
        roleRepository.deleteById(id);
    }
}

