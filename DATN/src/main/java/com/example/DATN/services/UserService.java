package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.AuthProvider;
import com.example.DATN.constant.PredefinedRole;
import com.example.DATN.dtos.request.user.RegisterRequest;
import com.example.DATN.dtos.request.user.UpdateUserRequest;
import com.example.DATN.dtos.respone.cart.CartResponse;
import com.example.DATN.dtos.respone.user.UserResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.CartMapper;
import com.example.DATN.mapper.UserMapper;
import com.example.DATN.models.Cart;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CartRepository;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    private final CartRepository cartRepository;

    private final GetUserByJwtHelper getUserByJwtHelper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    CartService cartService;
    UserRepository userRepository;
    SnowflakeIdGenerator snowflakeIdGenerator;
     CartMapper cartMapper;
    UserMapper userMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @Transactional(rollbackOn = Exception.class)
    public UserResponse createUser(RegisterRequest registerRequest) {
        User user = new User();
        CartResponse response=cartService.createCartForUser();
        Cart cart = cartMapper.toEntity(response);
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        Role adminRole = roleRepository.findByName(PredefinedRole.USER.name())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_EXIST));

        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setDob(registerRequest.getDob());
        user.setEmail(registerRequest.getEmail());
        user.setGuest(false);
        user.setProvider(AuthProvider.LOCAL);
        var roles = new HashSet<Role>();
        roles.add(adminRole);
        user.setRoles(roles);
        try {
            user.setCart(cart);
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new ApplicationException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        userMapper.updateUser(user, updateRequest);
        user.setUsername(updateRequest.getUsername());
        user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        var roles = roleRepository.findAllById(updateRequest.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        UserResponse userResponse = userMapper.toUserResponse(user);
        userRepository.deleteById(id);
        return userResponse;
    }

    public UserResponse getmyinfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserById(Long id) {
        User username = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(username);
    }
}

