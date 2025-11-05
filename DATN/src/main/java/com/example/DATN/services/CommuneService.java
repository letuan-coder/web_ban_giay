package com.example.DATN.services;

import com.example.DATN.dtos.respone.georaphy.CommuneResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.CommuneMapper;
import com.example.DATN.models.Commune;
import com.example.DATN.repositories.CommuneRepository;
import com.example.DATN.repositories.DistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommuneService {

    private final CommuneRepository communeRepository;
    private final CommuneMapper communeMapper;
    private final DistrictRepository districtRepository;

    public List<CommuneResponse> getAllCommunes() {
        return communeRepository.findAll().stream()
                .map(communeMapper::toCommuneResponse)
                .collect(Collectors.toList());
    }

    public List<CommuneResponse> getCommunesByDistrictCode(String districtCode) {
        if(!districtRepository.existsByCode(districtCode)){
            throw new ApplicationException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        List<Commune> communes = communeRepository.findByParentCode(districtCode);
        return communes.stream()
                .map(communeMapper::toCommuneResponse).toList();
    }
}
