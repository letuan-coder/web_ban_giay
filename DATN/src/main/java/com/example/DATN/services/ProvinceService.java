package com.example.DATN.services;

import com.example.DATN.dtos.respone.georaphy.ProvinceResponse;
import com.example.DATN.mapper.ProvinceMapper;
import com.example.DATN.repositories.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final ProvinceMapper provinceMapper;

    public List<ProvinceResponse> getAllProvinces() {
        return provinceRepository.findAll().stream()
                .map(provinceMapper::toProvinceResponse)
                .collect(Collectors.toList());
    }

    public Optional<ProvinceResponse> getProvinceById(String id) {
        return provinceRepository.findById(id)
                .map(provinceMapper::toProvinceResponse);
    }
}
