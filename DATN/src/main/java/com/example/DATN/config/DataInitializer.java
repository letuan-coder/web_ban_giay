package com.example.DATN.config;

import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;


    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        log.info("==================================================");
        log.info("Master Data Initialization Started...");
        try {
            initializeGeography();
            initializeRolesAndPermissions();
        } catch (Exception e) {
            log.error("Error during data initialization", e);
        }
        log.info("Master Data Initialization Finished.");
        log.info("==================================================");
    }

    private void initializeGeography() throws Exception {
        if (provinceRepository.count() > 0) {
            log.info("Geography data already initialized.");
            return;
        }

        Resource provincesResource = resourceLoader.getResource("classpath:data/georaphy/provinces.json");
        Map<String, GeoDTO> provinceDTOs = objectMapper.readValue(provincesResource.getInputStream(), new TypeReference<>() {});
        Map<String, Province> persistedProvinces = provinceDTOs.values().stream().map(dto -> {
            Province province = new Province();
            province.setId(dto.getCode());
            province.setCode(dto.getCode());
            province.setName(dto.getName());
            province.setSlug(dto.getSlug());
            province.setType(dto.getType());
            province.setNameWithType(dto.getName_with_type());
            return provinceRepository.save(province);
        }).collect(Collectors.toMap(Province::getCode, Function.identity()));
        log.info("Initialized {} provinces.", persistedProvinces.size());

        Resource districtsResource = resourceLoader.getResource("classpath:data/georaphy/districts.json");
        Map<String, GeoDTO> districtDTOs = objectMapper.readValue(districtsResource.getInputStream(), new TypeReference<>() {});
        Map<String, District> persistedDistricts = districtDTOs.values().stream().map(dto -> {
            Province parent = persistedProvinces.get(dto.getParent_code());
            if (parent == null) return null;
            District district = new District();
            district.setId(dto.getCode());
            district.setCode(dto.getCode());
            district.setName(dto.getName());
            district.setSlug(dto.getSlug());
            district.setType(dto.getType());
            district.setNameWithType(dto.getName_with_type());
            district.setPath(dto.getPath());
            district.setPathWithType(dto.getPath_with_type());
            district.setParentCode(dto.getParent_code());
            district.setProvince(parent);
            return districtRepository.save(district);
        }).filter(java.util.Objects::nonNull).collect(Collectors.toMap(District::getCode, Function.identity()));
        log.info("Initialized {} districts.", persistedDistricts.size());

        Resource communesResource = resourceLoader.getResource("classpath:data/georaphy/communes.json");
        Map<String, GeoDTO> communeDTOs = objectMapper.readValue(communesResource.getInputStream(), new TypeReference<>() {});
        communeDTOs.values().forEach(dto -> {
            District parent = persistedDistricts.get(dto.getParent_code());
            if (parent != null) {
                Commune commune = new Commune();
                commune.setId(dto.getCode());
                commune.setCode(dto.getCode());
                commune.setName(dto.getName());
                commune.setSlug(dto.getSlug());
                commune.setType(dto.getType());
                commune.setNameWithType(dto.getName_with_type());
                commune.setPath(dto.getPath());
                commune.setPathWithType(dto.getPath_with_type());
                commune.setParentCode(dto.getParent_code());
                commune.setDistrict(parent);
                communeRepository.save(commune);
            }
        });
        log.info("Initialized {} communes.", communeDTOs.size());
    }


    private void initializeRolesAndPermissions() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:data/role_and_permission/role_and_permission.json");
        RolePermissionConfig config = objectMapper.readValue(resource.getInputStream(), RolePermissionConfig.class);

        Map<String, Permission> persistedPermissions = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getName, Function.identity()));
        if (config.getPermissions() != null) {
            config.getPermissions().forEach(pDto -> persistedPermissions.computeIfAbsent(pDto.getCode(), code -> {
                Permission newPermission = new Permission();
                newPermission.setName(code);
                newPermission.setDescription(pDto.getDescription());
                return permissionRepository.save(newPermission);
            }));
        }
        log.info("Permissions checked/initialized.");

        Map<String, Role> persistedRoles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getName, Function.identity()));
        if (config.getRoles() != null) {
            config.getRoles().forEach(rDto -> persistedRoles.computeIfAbsent(rDto.getRole(), roleName -> {
                Role newRole = new Role();
                newRole.setName(roleName);
                newRole.setDescription(rDto.getDescription());
                return roleRepository.save(newRole);
            }));
        }
        log.info("Roles checked/initialized.");

        if (config.getRole_permissions() != null) {
            config.getRole_permissions().forEach(rpDto -> {
                Role role = persistedRoles.get(rpDto.getRole());
                if (role != null) {
                    final Set<Permission> finalPermissions = (role.getPermissions() == null) ? new HashSet<>() : role.getPermissions();
                    if (rpDto.getPermissions() != null) {
                        rpDto.getPermissions().forEach(permissionCode -> {
                            Permission permission = persistedPermissions.get(permissionCode);
                            if (permission != null) {
                                finalPermissions.add(permission);
                            }
                        });
                    }
                    role.setPermissions(finalPermissions);
                    roleRepository.save(role);
                }
            });
        }
        log.info("Role-Permission assignments checked/completed.");
    }

    @Data private static class RolePermissionConfig { private List<PermissionDTO> permissions; private List<RoleDTO> roles; private List<RolePermissionDTO> role_permissions; }
    @Data private static class PermissionDTO { private String code; private String description; }
    @Data private static class RoleDTO { private String role; private String description; }
    @Data private static class RolePermissionDTO { private String role; private List<String> permissions; }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeoDTO {
        private String name;
        private String code;
        private String parent_code;
        private String slug;
        private String type;
        private String name_with_type;
        private String path;
        private String path_with_type;
    }
}