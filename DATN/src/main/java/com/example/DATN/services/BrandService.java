package com.example.DATN.services;

import com.example.DATN.dtos.request.brand.BrandRequest;
import com.example.DATN.dtos.respone.brand.BrandResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.FormatInputString;
import com.example.DATN.mapper.BrandMapper;
import com.example.DATN.models.Brand;
import com.example.DATN.repositories.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final FormatInputString formatInputString;
    public BrandResponse createBrand(BrandRequest request) {
        Brand brand =Brand.builder()
                .name(formatInputString.formatInputString(request.getName()))
                .build();
        brand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(brand);
    }

    public List<BrandResponse> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toBrandResponse)
                .collect(Collectors.toList());
    }

    public List<BrandResponse> searchBrandsByName(String name) {
        return brandRepository.findByNameContainingIgnoreCase(name).stream()
                .map(brandMapper::toBrandResponse)
                .collect(Collectors.toList());
    }

    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));
        return brandMapper.toBrandResponse(brand);
    }

    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));
        request.setName(formatInputString.formatInputString(request.getName()));
        brandMapper.updateBrand(brand, request);
        brand = brandRepository.save(brand);
        return brandMapper.toBrandResponse(brand);
    }

    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));
        brandRepository.delete(brand);
    }
}