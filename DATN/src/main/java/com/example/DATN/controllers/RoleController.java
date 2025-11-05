package com.example.DATN.controllers;

import com.example.DATN.dtos.request.user.RoleRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.user.RoleResponse;
import com.example.DATN.services.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/role")
@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RoleController {
    final RoleService roleService;

    @GetMapping
    ApiResponse<List<RoleResponse>> GetAllRole(){
        return ApiResponse.<List<RoleResponse>>builder()
                .data(roleService.getAllRoles())
                .build();
    }
    @PostMapping
    ApiResponse<RoleResponse> CreateRole(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .data(roleService.createRole(request))
                .build();
    }
    @DeleteMapping("/{role}")
    ApiResponse<Void> CreateRole(@PathVariable String role){
        roleService.deleteRole(role);
        return ApiResponse.<Void>builder()
                .build();
    }
}
