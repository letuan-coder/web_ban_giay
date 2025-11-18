package com.example.DATN.controllers;

import com.example.DATN.dtos.request.user.RegisterRequest;
import com.example.DATN.dtos.request.user.UpdateUserRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.user.UserResponse;
import com.example.DATN.models.User;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(request))
                .message("Đăng ký thành công")
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getAllUsers())
                .build();
    }
    @GetMapping("/myinfo")
    ApiResponse<UserResponse> myinfo(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities();
        authentication.getName();
        return ApiResponse.<UserResponse>builder()
                .data(userService.getmyinfo())
                .build();
    }


    @DeleteMapping("/{id}")
    public ApiResponse<UserResponse> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với id: " + id));
        return ApiResponse.<UserResponse>builder()
                .data(userService.deleteUser(id))
                .message("Xóa thành công người dùng"+" "+user.getUsername())
                .build();
    }
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateProfile(
            @PathVariable Long id, @RequestBody @Valid UpdateUserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUser(id, request))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUserById(id))
                .build();
    }
}
