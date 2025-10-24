package com.example.DATN.config;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.PredefinedRole;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.repositories.RoleRepository;
import com.example.DATN.repositories.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j

public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;
    SnowflakeIdGenerator snowflakeIdGenerator;
    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                if (roleRepository.findByName(PredefinedRole.ADMIN.name()).isEmpty()) {
                    Role role = Role.builder()
                            .name(PredefinedRole.ADMIN.name())
                            .description("quyền quản trị hệ thống")
                            .build();
                    roleRepository.save(role);
                }

                log.info("Admin user not found, creating default admin user");

                User adminUser = new User();
                adminUser.setId(snowflakeIdGenerator.nextId());
                adminUser.setFirstName("Admin");
                adminUser.setLastName("Role");
                adminUser.setDob(java.time.LocalDate.now());
                adminUser.setEmail("example@email.com");
                adminUser.setUsername(ADMIN_USER_NAME);
                adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                var adminRole = roleRepository.findByName(PredefinedRole.ADMIN.name())
                        .orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_EXIST));
                var roles = new HashSet<Role>();
                roles.add(adminRole);
                adminUser.setRoles(roles);
                userRepository.save(adminUser);
                System.out.println("Default admin user created with username: " + ADMIN_USER_NAME + " and password: " + ADMIN_PASSWORD);
                log.info("Default admin user created with username: {} and password: {}", ADMIN_USER_NAME, ADMIN_PASSWORD);
            }
            else {
                log.info("Admin user already exists, skipping creation");
            }
        };
    }
}
