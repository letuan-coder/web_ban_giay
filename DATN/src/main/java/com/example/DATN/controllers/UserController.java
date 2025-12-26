package com.example.DATN.controllers;

import com.example.DATN.dtos.request.user.RegisterRequest;
import com.example.DATN.dtos.request.user.UpdatePasswordRequest;
import com.example.DATN.dtos.request.user.UpdateUserRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.user.UserDetailResponse;
import com.example.DATN.dtos.respone.user.UserResponse;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Validated
@Slf4j
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    ApiResponse<UserResponse> createUser(
            @RequestBody @Valid RegisterRequest request) {
        userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .data(null)
                .message("Đăng ký thành công")
                .build();
    }

    @GetMapping("my-orders")
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getAllUsers())
                .build();
    }

    @GetMapping("/myinfo")
    ApiResponse<UserDetailResponse> myinfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities();
        authentication.getName();
        return ApiResponse.<UserDetailResponse>builder()
                .data(userService.getmyinfo())
                .build();
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.<Void>builder()
                .data(null)
                .message("Xóa thành công người dùng")
                .build();
    }

    @PatchMapping
    public ApiResponse<UserDetailResponse> updateProfile(
            @RequestBody @Valid UpdateUserRequest request) {
        return ApiResponse.<UserDetailResponse>builder()
                .data(userService.updateUser(request))
                .build();
    }

    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<UserDetailResponse> uploadImageProfile(
            @RequestPart(value = "file", required = false) MultipartFile file) {
        UserDetailResponse response = userService.UploadUserImage(file);
        return ApiResponse.<UserDetailResponse>builder()
                .data(null)
                .message("upload avatar successfully")
                .build();
    }

    @PatchMapping("/password")
    public ApiResponse<Void> updatePassword(
            @RequestBody @Valid UpdatePasswordRequest request) {
        userService.updatePassword(request);
        return ApiResponse.<Void>builder()
                .data(null)
                .message("update password successfully !!")
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUserById(id))
                .build();
    }
}
