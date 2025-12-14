package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.AuthProvider;
import com.example.DATN.constant.PredefinedRole;
import com.example.DATN.dtos.request.UploadImageRequest;
import com.example.DATN.dtos.request.user.RegisterRequest;
import com.example.DATN.dtos.request.user.UpdatePasswordRequest;
import com.example.DATN.dtos.request.user.UpdateUserRequest;
import com.example.DATN.dtos.respone.user.UserDetailResponse;
import com.example.DATN.dtos.respone.user.UserResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.UserMapper;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    private final CartRepository cartRepository;

    private final GetUserByJwtHelper getUserByJwtHelper;
    private final ImageProductService imageProductService;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    FileStorageService fileStorageService;
    SnowflakeIdGenerator snowflakeIdGenerator;
    UserMapper userMapper;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse).toList();
    }

    @Transactional(rollbackOn = Exception.class)
    public UserResponse createUser(RegisterRequest registerRequest) {
        User user = new User();
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ApplicationException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Long newId = snowflakeIdGenerator.nextId();
        user.setId(newId);
        if (!registerRequest.getPassword().equals(registerRequest.getPassword())) {
            throw new ApplicationException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }
        Role role = null;
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        Optional<Role> adminRoleOpt = roleRepository.findByName(PredefinedRole.USER.name());
        if (!adminRoleOpt.isPresent()) {
            role = Role.builder()
                    .name(PredefinedRole.USER.name())
                    .description("user role")
                    .permissions(null)
                    .build();
            roleRepository.save(role);
        } else {
            role = adminRoleOpt.get();
        }
        user.setUserImage("");
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setDob(registerRequest.getDob());
        user.setEmail(registerRequest.getEmail());
        user.setProvider(AuthProvider.LOCAL);
        var roles = new HashSet<Role>();
        roles.add(role);
        user.setRoles(roles);
        user.setOrders(new ArrayList<>());
        try {
            user = userRepository.save(user);
//            Cart cartMerge = cartService.MergeCartForUser(user);
            userRepository.save(user);
//            user.setCart(cartMerge);

        } catch (DataIntegrityViolationException exception) {
            throw new ApplicationException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        return userMapper.toUserResponse(user);
    }

    @Transactional(rollbackOn = Exception.class)
    public UserDetailResponse updateUser(
            UpdateUserRequest updateRequest) {
        User user = getUserByJwtHelper.getCurrentUser();
        userMapper.updateUser(user, updateRequest);
        user.setFirstName(updateRequest.getFirstName());
        user.setLastName(updateRequest.getLastName());
        return userMapper.toUserDetailResponse(userRepository.save(user));
    }
    public UserDetailResponse UploadUserImage(MultipartFile file) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (user.getUserImage()!=null && user.getUserImage()!=""){
            fileStorageService.deleteFile(user.getUserImage());
            user.setUserImage("");
            userRepository.save(user);
        }
        UploadImageRequest uploadImageRequest = UploadImageRequest.builder()
                .imageUrl("avatar"+user.getId())
                .file(file)
                .userAvatar(user)
                .altText("avatar"+user.getUsername())
                .build();
        imageProductService.uploadImage(uploadImageRequest);
       return userMapper.toUserDetailResponse(userRepository.save(user));
    }

    @Transactional(rollbackOn = Exception.class)
    public void updatePassword(UpdatePasswordRequest updateRequest) {
        User user = getUserByJwtHelper.getCurrentUser();
        if (passwordEncoder.matches(updateRequest.getPassword(), user.getPassword())) {
            throw new ApplicationException(ErrorCode.PASSWORD_MATCHED);
        } else {
            if (!passwordEncoder.matches(updateRequest.getOldPassword(), user.getPassword())) {
                throw new ApplicationException(ErrorCode.PASSWORD_NOT_MATCH);
            }
            if (!updateRequest.getPassword().equals(updateRequest.getConfirmPassword())) {
                throw new ApplicationException(ErrorCode.PASSWORD_NOT_MATCH);

            }
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }

    public UserDetailResponse getmyinfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserDetailResponse(user);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserById(Long id) {
        User username = userRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        return userMapper.toUserResponse(username);
    }
}

