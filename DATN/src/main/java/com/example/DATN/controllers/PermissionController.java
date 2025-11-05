package com.example.DATN.controllers;

import com.example.DATN.dtos.request.user.PermissionRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.user.PermissionResponse;
import com.example.DATN.services.PermissionService;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/permission")
@AllArgsConstructor
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request){
        return ApiResponse.<PermissionResponse>builder()
                .data(permissionService.Create(request))
                .build();
    }
    @GetMapping
    ApiResponse<List<PermissionResponse>> GetAllPermission() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .data(permissionService.GetAllPermission())
                .build();
    }
    @DeleteMapping("/{permission}")
    ApiResponse<Void> DeletePermsision(@PathVariable String name){
        permissionService.DeletePermission(name);
      return ApiResponse.<Void>builder().build();
    }
}
