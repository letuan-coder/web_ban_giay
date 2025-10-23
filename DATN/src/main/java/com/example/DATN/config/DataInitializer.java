package com.example.DATN.config;

import com.example.DATN.models.*;
import com.example.DATN.repositories.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final CommuneRepository communeRepository;
    private final ObjectMapper objectMapper;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        long provinceCount = provinceRepository.count();
        long districtCount = districtRepository.count();
        long communeCount = communeRepository.count();
        long colorCount = colorRepository.count();
        long sizeCount = sizeRepository.count();
        if (provinceCount == 0 && districtCount == 0 && communeCount == 0) {
            log.info("Seeding address data...");

            // 1. Seed Provinces
            Map<String, Province> provinceMap = seedProvinces();

            // 2. Seed Districts
            Map<String, District> districtMap = seedDistricts(provinceMap);

            // 3. Seed Communes
            seedCommunes(districtMap);

            log.info("Finished seeding address data.");
        } else {
            log.info("Address data already exists. Skipping seed.");
        }
        if (sizeCount == 0 || colorCount == 0) {
            log.info("Seeding size and color data...");
            seedColors();
            seedSizes();
            log.info("Finished seeding size and color data.");
        } else {
            log.info("Size and color data already exists. Skipping seed.");
        }
    }

    private Map<String, Province> seedProvinces() throws Exception {
        TypeReference<Map<String, AddressNode>> typeRef = new TypeReference<>() {
        };
        InputStream inputStream = new ClassPathResource("data/georaphy/provinces.json").getInputStream();
        Map<String, AddressNode> provinceNodes = objectMapper.readValue(inputStream, typeRef);

        List<Province> provincesToSave = provinceNodes.values().stream()
                .map(node -> {
                    Province p = new Province();
                    p.setId(node.getCode()); // Set ID from code
                    p.setCode(node.getCode());
                    p.setName(node.getName());
                    p.setSlug(node.getSlug());
                    p.setType(node.getType());
                    p.setNameWithType(node.getNameWithType());
                    return p;
                }).collect(Collectors.toList());

        provinceRepository.saveAll(provincesToSave);
        return provincesToSave.stream().collect(Collectors.toMap(Province::getCode, Function.identity()));
    }

    private Map<String, District> seedDistricts(Map<String, Province> provinceMap) throws Exception {
        TypeReference<Map<String, AddressNode>> typeRef = new TypeReference<>() {
        };
        InputStream inputStream = new ClassPathResource("data/georaphy/districts.json").getInputStream();
        Map<String, AddressNode> districtNodes = objectMapper.readValue(inputStream, typeRef);

        List<District> districtsToSave = districtNodes.values().stream()
                .map(node -> {
                    Province parent = provinceMap.get(node.getParentCode());
                    if (parent == null) return null;
                    District d = new District();
                    d.setId(node.getCode()); // Set ID from code
                    d.setCode(node.getCode());
                    d.setName(node.getName());
                    d.setSlug(node.getSlug());
                    d.setType(node.getType());
                    d.setNameWithType(node.getNameWithType());
                    d.setPath(node.getPath());
                    d.setPathWithType(node.getPathWithType());
                    d.setParentCode(node.getParentCode());
                    d.setProvince(parent);
                    return d;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        districtRepository.saveAll(districtsToSave);
        return districtsToSave.stream().collect(Collectors.toMap(District::getCode, Function.identity()));
    }

    private void seedCommunes(Map<String, District> districtMap) throws Exception {
        TypeReference<Map<String, AddressNode>> typeRef = new TypeReference<>() {
        };
        InputStream inputStream = new ClassPathResource("data/georaphy/communes.json").getInputStream();
        Map<String, AddressNode> communeNodes = objectMapper.readValue(inputStream, typeRef);

        List<Commune> communesToSave = communeNodes.values().stream()
                .map(node -> {
                    District parent = districtMap.get(node.getParentCode());
                    if (parent == null) return null;
                    Commune c = new Commune();
                    c.setId(node.getCode()); // Set ID from code
                    c.setCode(node.getCode());
                    c.setName(node.getName());
                    c.setSlug(node.getSlug());
                    c.setType(node.getType());
                    c.setNameWithType(node.getNameWithType());
                    c.setPath(node.getPath());
                    c.setPathWithType(node.getPathWithType());
                    c.setParentCode(node.getParentCode());
                    c.setDistrict(parent);
                    return c;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        communeRepository.saveAll(communesToSave);
    }

    private void seedSizes() throws Exception {
        TypeReference<List<SizeNode>> typeRef = new TypeReference<>() {
        };
        InputStream inputStream = new ClassPathResource("data/size_and_color/sizes.json").getInputStream();
        List<SizeNode> sizeNodes = objectMapper.readValue(inputStream, typeRef);

        List<Size> sizesToSave = sizeNodes.stream()
                .map(node -> {
                    Size s = new Size();
                    s.setCode(node.getCode());
                    s.setName(node.getName());
                    return s;
                }).collect(Collectors.toList());

        sizeRepository.saveAll(sizesToSave);
    }

    private void seedColors() throws Exception {
        TypeReference<List<ColorNode>> typeRef = new TypeReference<>() {
        };
        InputStream inputStream = new ClassPathResource("data/size_and_color/colors.json").getInputStream();
        List<ColorNode> colorNodes = objectMapper.readValue(inputStream, typeRef);

        List<Color> colorsToSave = colorNodes.stream()
                .map(node -> {
                    Color c = new Color();
                    c.setCode(node.getCode());
                    c.setName(node.getName());
                    c.setHexCode(node.getHexCode());
                    return c;
                }).collect(Collectors.toList());

        colorRepository.saveAll(colorsToSave);
    }

    @Data
    static class AddressNode {
        private String name;
        private String type;
        private String slug;
        @JsonProperty("name_with_type")
        private String nameWithType;
        private String path;
        @JsonProperty("path_with_type")
        private String pathWithType;
        private String code;
        @JsonProperty("parent_code")
        private String parentCode;
        @JsonProperty("name_en")
        private String nameEn;
    }

    @Data
    static class ColorNode {
        private String code;
        private String name;
        private String hexCode;
    }

    @Data
    static class SizeNode {
        private String code;
        private String name;
    }
}
