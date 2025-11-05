package com.example.DATN.services;

import com.example.DATN.dtos.respone.georaphy.DistrictResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.DistrictMapper;
import com.example.DATN.models.District;
import com.example.DATN.repositories.DistrictRepository;
import com.example.DATN.repositories.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final DistrictMapper districtMapper;
    private final ProvinceRepository provinceRepository;
    public List<DistrictResponse> getAllDistricts() {
        return districtRepository.findAll().stream()
                .map(districtMapper::toDistrictResponse)
                .collect(Collectors.toList());
    }

    public List<DistrictResponse> getDistrictsByProvinceCode(String provinceCode) {
        if(!provinceRepository.existsByCode(provinceCode)){
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        List<District> districts = districtRepository.findByParentCode(provinceCode);
        return districts.stream()
                .map(districtMapper::toDistrictResponse).toList();
    }
}
